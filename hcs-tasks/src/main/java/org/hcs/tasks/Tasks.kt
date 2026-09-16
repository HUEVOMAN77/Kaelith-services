package org.hcs.tasks

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun interface OnSuccessListener<T> {
    fun onSuccess(result: T)
}

fun interface OnFailureListener {
    fun onFailure(e: Exception)
}

fun interface OnCompleteListener<T> {
    fun onComplete(task: Task<T>)
}

class CancellationTokenSource {
    private var isCancelled = false

    fun cancel() {
        isCancelled = true
    }

    val token: CancellationToken = object : CancellationToken {
        override val isCancellationRequested: Boolean
            get() = isCancelled
    }
}

interface CancellationToken {
    val isCancellationRequested: Boolean
}

abstract class Task<T> {
    abstract val isComplete: Boolean
    abstract val isSuccessful: Boolean
    abstract val isCanceled: Boolean
    abstract val result: T?
    abstract val exception: Exception?

    abstract fun addOnSuccessListener(executor: Executor, listener: OnSuccessListener<T>): Task<T>
    abstract fun addOnSuccessListener(listener: OnSuccessListener<T>): Task<T>

    abstract fun addOnFailureListener(executor: Executor, listener: OnFailureListener): Task<T>
    abstract fun addOnFailureListener(listener: OnFailureListener): Task<T>

    abstract fun addOnCompleteListener(executor: Executor, listener: OnCompleteListener<T>): Task<T>
    abstract fun addOnCompleteListener(listener: OnCompleteListener<T>): Task<T>
}

class TaskCompletionSource<T>(private val cancellationToken: CancellationToken? = null) {

    val task: Task<T> = HcsTask(cancellationToken)

    fun setResult(result: T) {
        (task as HcsTask<T>).setResult(result)
    }

    fun setException(e: Exception) {
        (task as HcsTask<T>).setException(e)
    }

    fun trySetResult(result: T): Boolean {
        return (task as HcsTask<T>).trySetResult(result)
    }

    fun trySetException(e: Exception): Boolean {
        return (task as HcsTask<T>).trySetException(e)
    }
}

internal class HcsTask<T>(private val cancellationToken: CancellationToken? = null) : Task<T>() {

    private val lock = Any()
    override var isComplete: Boolean = false
        private set
    override var isSuccessful: Boolean = false
        private set
    override var isCanceled: Boolean = false
        private set
    override var result: T? = null
        private set
    override var exception: Exception? = null
        private set

    private val successListeners = mutableListOf<Pair<Executor, OnSuccessListener<T>>>()
    private val failureListeners = mutableListOf<Pair<Executor, OnFailureListener>>()
    private val completeListeners = mutableListOf<Pair<Executor, OnCompleteListener<T>>>()

    fun setResult(result: T) {
        synchronized(lock) {
            check(!isComplete) { "Task is already complete" }
            if (cancellationToken?.isCancellationRequested == true) {
                isCanceled = true
                isComplete = true
                return
            }
            this.result = result
            this.isSuccessful = true
            this.isComplete = true
        }
        dispatchCallbacks()
    }

    fun trySetResult(result: T): Boolean {
        synchronized(lock) {
            if (isComplete) return false
            if (cancellationToken?.isCancellationRequested == true) {
                isCanceled = true
                isComplete = true
                return true
            }
            this.result = result
            this.isSuccessful = true
            this.isComplete = true
        }
        dispatchCallbacks()
        return true
    }

    fun setException(e: Exception) {
        synchronized(lock) {
            check(!isComplete) { "Task is already complete" }
            this.exception = e
            this.isSuccessful = false
            this.isComplete = true
        }
        dispatchCallbacks()
    }

    fun trySetException(e: Exception): Boolean {
        synchronized(lock) {
            if (isComplete) return false
            this.exception = e
            this.isSuccessful = false
            this.isComplete = true
        }
        dispatchCallbacks()
        return true
    }

    override fun addOnSuccessListener(executor: Executor, listener: OnSuccessListener<T>): Task<T> {
        synchronized(lock) {
            if (isComplete && isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                val res = result as T
                executor.execute { listener.onSuccess(res) }
            } else {
                successListeners.add(executor to listener)
            }
        }
        return this
    }

    override fun addOnSuccessListener(listener: OnSuccessListener<T>): Task<T> {
        return addOnSuccessListener(TaskExecutors.MAIN_THREAD, listener)
    }

    override fun addOnFailureListener(executor: Executor, listener: OnFailureListener): Task<T> {
        synchronized(lock) {
            if (isComplete && !isSuccessful && exception != null) {
                executor.execute { listener.onFailure(exception!!) }
            } else {
                failureListeners.add(executor to listener)
            }
        }
        return this
    }

    override fun addOnFailureListener(listener: OnFailureListener): Task<T> {
        return addOnFailureListener(TaskExecutors.MAIN_THREAD, listener)
    }

    override fun addOnCompleteListener(executor: Executor, listener: OnCompleteListener<T>): Task<T> {
        synchronized(lock) {
            if (isComplete) {
                executor.execute { listener.onComplete(this) }
            } else {
                completeListeners.add(executor to listener)
            }
        }
        return this
    }

    override fun addOnCompleteListener(listener: OnCompleteListener<T>): Task<T> {
        return addOnCompleteListener(TaskExecutors.MAIN_THREAD, listener)
    }

    private fun dispatchCallbacks() {
        val currentResult = result
        val currentException = exception
        val currentIsSuccess = isSuccessful

        val pendingSuccess: List<Pair<Executor, OnSuccessListener<T>>>
        val pendingFailure: List<Pair<Executor, OnFailureListener>>
        val pendingComplete: List<Pair<Executor, OnCompleteListener<T>>>

        synchronized(lock) {
            pendingSuccess = ArrayList(successListeners)
            pendingFailure = ArrayList(failureListeners)
            pendingComplete = ArrayList(completeListeners)
            successListeners.clear()
            failureListeners.clear()
            completeListeners.clear()
        }

        if (currentIsSuccess) {
            @Suppress("UNCHECKED_CAST")
            val res = currentResult as T
            for ((exec, l) in pendingSuccess) {
                exec.execute { l.onSuccess(res) }
            }
        } else if (currentException != null) {
            for ((exec, l) in pendingFailure) {
                exec.execute { l.onFailure(currentException) }
            }
        }

        for ((exec, l) in pendingComplete) {
            exec.execute { l.onComplete(this) }
        }
    }
}

object TaskExecutors {
    val MAIN_THREAD: Executor = DirectExecutor
    val BACKGROUND: Executor = Executors.newCachedThreadPool()

    private object DirectExecutor : Executor {
        override fun execute(command: Runnable) {
            command.run()
        }
    }
}

object Tasks {
    fun <T> forResult(result: T): Task<T> {
        val source = TaskCompletionSource<T>()
        source.setResult(result)
        return source.task
    }

    fun <T> forException(e: Exception): Task<T> {
        val source = TaskCompletionSource<T>()
        source.setException(e)
        return source.task
    }

    fun <T> await(task: Task<T>, timeout: Long, unit: TimeUnit): T {
        if (task.isComplete) {
            if (task.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                return task.result as T
            }
            throw task.exception ?: IllegalStateException("Task failed without exception")
        }

        val latch = CountDownLatch(1)
        task.addOnCompleteListener(TaskExecutors.MAIN_THREAD) { latch.countDown() }
        val completed = latch.await(timeout, unit)
        if (!completed) throw java.util.concurrent.TimeoutException("Task timed out")

        if (task.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            return task.result as T
        }
        throw task.exception ?: IllegalStateException("Task failed without exception")
    }
}
