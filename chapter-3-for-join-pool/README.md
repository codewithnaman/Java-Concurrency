# Fork And Join Pool
## Why we need Fork Join Pool; Problem with Executor Pool 
We have executor service in Java; with an executor we can submit a task to pool, and it will execute the task as the 
pool have active slot. There are some situations where deadlock is induced due to these pools. Let's take an example where 
logically there is no chance of deadlock but deadlock happen because of Executor pool, or I can say Executor pool induce
the deadlock; and it becomes dependent with input and pool size. 

In example [ExecutorServiceInducedDeadLock](src/main/java/com/cwn/problem/ExecutorServiceInducedDeadLock.java); we have
logic to check prime numbers which is within method isPrime; Then we are finding the count of prime numbers between
range withing numberOfPrimeNumbersInRange and in one other method splitAndCompute we are checking if range difference 
is less than 100 then work in single threaded manner otherwise split it into two parts and run both in individual 
threads and get the result using callable; then sum it up and return. Now if we look at logic point of view the 
code does not have any deadlock.

If we vary the range and threadPoolSize; with the executor service then there will deadlock because of pool size too
big or pool size is very less. Because on line 'result = task1.get() + task2.get();' we are waiting for the result 
from other thread to come; if pool size is very less and range is big then it will split work much and after a point
it will not able to allocate slot for thread result is required to complete other thread. For Example: If we change the 
threadPoolSize to 10 and range is from 1 to 1000; then output will be:
```text
System Available Cores 16
Computing Prime number from 1 to 1000 Thread[main,5,main]
Computing Prime number from 1 to 500 Thread[pool-1-thread-1,5,main]
Computing Prime number from 501 to 1000 Thread[pool-1-thread-3,5,main]
Computing Prime number from 1 to 250 Thread[pool-1-thread-2,5,main]
Computing Prime number from 251 to 500 Thread[pool-1-thread-4,5,main]
Computing Prime number from 501 to 750 Thread[pool-1-thread-5,5,main]
Computing Prime number from 751 to 1000 Thread[pool-1-thread-6,5,main]
Computing Prime number from 1 to 125 Thread[pool-1-thread-7,5,main]
Computing Prime number from 251 to 375 Thread[pool-1-thread-8,5,main]
Computing Prime number from 501 to 625 Thread[pool-1-thread-10,5,main]
Computing Prime number from 126 to 250 Thread[pool-1-thread-9,5,main]
```
In above 1 to 125 range task has been splatted but not able to execute the task as no slot available to put an active 
thread into running thread as all running thread slots are already consumed and all waiting for other threads to 
complete which are in active state and not able to come in running state.

For the above problem let's say we increase the pool size to 100; then it will work and output will be:
```text
System Available Cores 16
Computing Prime number from 1 to 1000 Thread[main,5,main]
Computing Prime number from 1 to 500 Thread[pool-1-thread-1,5,main]
Computing Prime number from 1 to 250 Thread[pool-1-thread-2,5,main]
Computing Prime number from 501 to 1000 Thread[pool-1-thread-3,5,main]
Computing Prime number from 251 to 500 Thread[pool-1-thread-4,5,main]
Computing Prime number from 1 to 125 Thread[pool-1-thread-5,5,main]
Computing Prime number from 126 to 250 Thread[pool-1-thread-6,5,main]
Computing Prime number from 251 to 375 Thread[pool-1-thread-8,5,main]
Computing Prime number from 501 to 750 Thread[pool-1-thread-7,5,main]
Computing Prime number from 1 to 63 Thread[pool-1-thread-10,5,main]
Computing Prime number from 1 to 63 Completed Thread[pool-1-thread-10,5,main]
Computing Prime number from 376 to 500 Thread[pool-1-thread-9,5,main]
Computing Prime number from 751 to 1000 Thread[pool-1-thread-11,5,main]
Computing Prime number from 501 to 625 Thread[pool-1-thread-15,5,main]
Computing Prime number from 64 to 125 Thread[pool-1-thread-13,5,main]
Computing Prime number from 64 to 125 Completed Thread[pool-1-thread-13,5,main]
Computing Prime number from 376 to 438 Thread[pool-1-thread-17,5,main]
Computing Prime number from 626 to 750 Thread[pool-1-thread-16,5,main]
Computing Prime number from 1 to 125 Completed Thread[pool-1-thread-5,5,main]
Computing Prime number from 376 to 438 Completed Thread[pool-1-thread-17,5,main]
Computing Prime number from 126 to 188 Thread[pool-1-thread-12,5,main]
Computing Prime number from 189 to 250 Thread[pool-1-thread-21,5,main]
Computing Prime number from 126 to 188 Completed Thread[pool-1-thread-12,5,main]
Computing Prime number from 189 to 250 Completed Thread[pool-1-thread-21,5,main]
Computing Prime number from 439 to 500 Thread[pool-1-thread-20,5,main]
Computing Prime number from 251 to 313 Thread[pool-1-thread-14,5,main]
Computing Prime number from 439 to 500 Completed Thread[pool-1-thread-20,5,main]
Computing Prime number from 251 to 313 Completed Thread[pool-1-thread-14,5,main]
Computing Prime number from 876 to 1000 Thread[pool-1-thread-24,5,main]
Computing Prime number from 751 to 875 Thread[pool-1-thread-18,5,main]
Computing Prime number from 501 to 563 Thread[pool-1-thread-19,5,main]
Computing Prime number from 126 to 250 Completed Thread[pool-1-thread-6,5,main]
Computing Prime number from 626 to 688 Thread[pool-1-thread-22,5,main]
Computing Prime number from 376 to 500 Completed Thread[pool-1-thread-9,5,main]
Computing Prime number from 876 to 938 Thread[pool-1-thread-26,5,main]
Computing Prime number from 1 to 250 Completed Thread[pool-1-thread-2,5,main]
Computing Prime number from 501 to 563 Completed Thread[pool-1-thread-19,5,main]
Computing Prime number from 876 to 938 Completed Thread[pool-1-thread-26,5,main]
Computing Prime number from 751 to 813 Thread[pool-1-thread-28,5,main]
Computing Prime number from 626 to 688 Completed Thread[pool-1-thread-22,5,main]
Computing Prime number from 939 to 1000 Thread[pool-1-thread-29,5,main]
Computing Prime number from 564 to 625 Thread[pool-1-thread-25,5,main]
Computing Prime number from 939 to 1000 Completed Thread[pool-1-thread-29,5,main]
Computing Prime number from 814 to 875 Thread[pool-1-thread-30,5,main]
Computing Prime number from 751 to 813 Completed Thread[pool-1-thread-28,5,main]
Computing Prime number from 689 to 750 Thread[pool-1-thread-27,5,main]
Computing Prime number from 814 to 875 Completed Thread[pool-1-thread-30,5,main]
Computing Prime number from 314 to 375 Thread[pool-1-thread-23,5,main]
Computing Prime number from 751 to 875 Completed Thread[pool-1-thread-18,5,main]
Computing Prime number from 314 to 375 Completed Thread[pool-1-thread-23,5,main]
Computing Prime number from 689 to 750 Completed Thread[pool-1-thread-27,5,main]
Computing Prime number from 876 to 1000 Completed Thread[pool-1-thread-24,5,main]
Computing Prime number from 564 to 625 Completed Thread[pool-1-thread-25,5,main]
Computing Prime number from 751 to 1000 Completed Thread[pool-1-thread-11,5,main]
Computing Prime number from 626 to 750 Completed Thread[pool-1-thread-16,5,main]
Computing Prime number from 251 to 375 Completed Thread[pool-1-thread-8,5,main]
Computing Prime number from 501 to 625 Completed Thread[pool-1-thread-15,5,main]
Computing Prime number from 251 to 500 Completed Thread[pool-1-thread-4,5,main]
Computing Prime number from 501 to 750 Completed Thread[pool-1-thread-7,5,main]
Computing Prime number from 1 to 500 Completed Thread[pool-1-thread-1,5,main]
Computing Prime number from 501 to 1000 Completed Thread[pool-1-thread-3,5,main]
Computing Prime number from 1 to 1000 Completed Thread[main,5,main]
Prime numbers between Start : 1 End : 1000 is 168
```

We have enough pool size depending on the above problem. But What if the range changes from 1 to 100000; then it will
have same problem; so here we are not able to predict the pool size depending on the range.

This is not logical problem from code; this is something induced depending on the pool size and inputs. 

### When we encounter the pool induced deadlock and need to choose for Fork And Join Pool
These type of problems we see in the divide and conquer type of problems; which are recursive in nature; Where parent
task has dependency on completion of the splatted task.

## ForkJoin Pool
ForkJoin pool solves our above problem by a concept called work stealing. Work stealing give a chance to run the task 
in pool in place of the task which is waiting for the other thread to complete its task. Lets consider there is pool of 
3 threads in which task A is waiting to completing of the task B,C,D; and B,C,D also have child task to complete. 
Since; A is waiting for completion of B,C,D and pool is of 3 threads only so in executor service; only any 3 threads 
can be run in parallel, and A is parent thread so from B,C,D any 2 thread will run and then deadlock happen because A 
need B,C,D results to complete and B,C,D need to their child tasks to complete. While in ForkJoinPool any task which is 
waiting for any other task or thread to complete put in waiting state and thread is assigned to other task to complete 
the work; This concept is called work stealing. For our example in ForkJoin Pool since A is waiting to complete B,C,D;
so it will goes in waiting and thread is assigned to other task might be D or child task of B,C,D; or B,C are also waiting
for their child to complete; so, B,C also can be go in waiting queue, and their child task run and complete. When their 
child task completed then task B,C,D can be assigned to thread in pool; and they can complete their task; and then
A can complete its task depending on the result returned by B,C,D.

In ForkJoinPool only those thread sent to waiting state which are waiting for other threads to complete; not those
thread which are waiting for the IO, Network response or other resource to free.

### ForkJoinPool Creation
Before we go in detail how the ForkJoinPool solves our problem; lets understand how to create and submit a task to 
ForkJoinPool.

To create a ForkJoinPool we can get a common pool using below line:
```text
      ForkJoinPool pool = ForkJoinPool.commonPool();
      System.out.println(pool);
```
If we see output of the above it looks like below:
```text
java.util.concurrent.ForkJoinPool@1540e19d[Running, parallelism = 15, size = 0, active = 0, running = 0, steals = 0, tasks = 0, submissions = 0]
```
Where parallelism 15 shows that it can run 15 threads in parallel. When we use commonpool method to get ForkJoinPool it 
gives by default n-1 thread ForkJoinPool where n is number of logical cores available on the machine. Like on which 
machine example run have 16 cores, so it creates the 15 thread pool.

We can submit runnable task to pool like below:
```text
    pool.submit(() -> task(index));
```

The above pool is of the ForkJoinPool is common pool which has fixed size depending on the number of logical cores 
available on system. If we need custom size ForkJoinPool then we can create like below, with size of pool which we
require:
```text
        ForkJoinPool pool = new ForkJoinPool(); //Create pool with n thread; n is number of logical cores
        ForkJoinPool pool = new ForkJoinPool(100); // Create pool with 100 threads 
```

There are differences between the task submission to pool depending on task submitter is outside the pool thread;
or task is submitted within the pool i.e. thread running within the pool itself and submit task. There are different
methods to submit the tasks to pool depending on the use case.

### ForkJoinPool and Runnable or asynchronous task
Let's see how many ways we can submit the runnable or asynchronously task to ForkJoinPool:
```text
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("Sending task");
        pool.execute(()->task());
        pool.execute(ForkJoinPoolRunnableDifferentMethods::task);
        ForkJoinTask task = ForkJoinTask.adapt(ForkJoinPoolRunnableDifferentMethods::task);
        pool.execute(task);
        System.out.println("Done");
        pool.awaitTermination(10, TimeUnit.SECONDS);
```
In above example we can submit the task using execute method. We can provide Runnable implementation, lambda for the 
Runnable or method reference. We can also submit the ForkJoinTask of Runnable to pool. The ForkJoinTask of Runnable can
be created using the ForkJoinTask adapt method.

ForkJoinTask can be forked to submit the task from itself to pool and then join at the end of execution; which we will 
look in upcoming next sections. ForkJoinTask is an abstract class, and it implements the Future; The implementation for
this class is RecursiveAction and RecursiveTask; which usage we will see in upcoming sections.

ForkJoinPool execute has return type void; it means it is for those task for which we want to run asynchronously and 
not interested in result after completion of task.

### ForkJoinPool and Callable
If we are interested in the result returned by a task after it completes its execution. For this kind of tasks we use 
Callable in ExecutorService. Let's see how we can accommodate this within ForkJoinPool.

We will submit a Callable task to ForkJoinPool using invoke; but invoke just take the ForkJoinTask not the Callable 
implementation as argument. But, we can convert a Callable task easily into the ForkJoinTask using adapt method of 
ForkJoinTask which takes callable as the argument and creates ForkJoinTask. 
```text
        System.out.println("Start");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ForkJoinTask<Integer> task = ForkJoinTask.adapt(ForkJoinPoolWithCallable::compute);
        Integer result = pool.invoke(task);
        System.out.println("Done " + result);
        pool.awaitTermination(10, TimeUnit.SECONDS);
```
 
