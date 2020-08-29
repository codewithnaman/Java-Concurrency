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

Above call is synchronous call; i.e. the task is running in a different thread but waiting for completing the thread and
getting the result. While we generally look for that task execute asynchronously to take advantage of the 
multi-threading and once task will be completed then we get the result from it. So, when we should use invoke method; 
invoke method is useful when we need to perform the complex task which can be solved using divide and conquer approach,
and you need result immediately after completion of result then we use invoke method.

So what we need to do if we need to put the task as asynchronous and perform some other task in between and get result
when required. For that ForkJoinPool provides submit method which takes a Callable or ForkJoinTask and returns
a ForkJoinTask; Since ForkJoinTask implements Future; It will return result once task execution completes. Let's see
Example of submit:
```text
 System.out.println("Start");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ForkJoinTask<Integer> result = pool.submit(ForkJoinPoolWithCallableAsynchronous::compute);
        System.out.println("Doing some other work");
        Thread.sleep(2000);
        System.out.println("Other Work Done");
        System.out.println("First task Done " + result.get());

        ForkJoinTask<Integer> task = ForkJoinTask.adapt(ForkJoinPoolWithCallableAsynchronous::compute);
        ForkJoinTask<Integer> result2 = pool.submit(task);
        System.out.println("Doing some other work");
        Thread.sleep(2000);
        System.out.println("Other Work Done");
        System.out.println("Second task Done " + result2.get());
        pool.awaitTermination(10, TimeUnit.SECONDS);
```
In above snippet we submit the task to ForkJoinPool and then started other work; once we completed the other work and 
need the result of the task we submitted to ForkJoinPool then we can call get method to get the result.

Till this section we have seen we have 3 methods to submit a task to ForkJoinPool from outside or main thread:
1. execute - Takes Runnable or ForkJoinTask and does not return any result 
[ForkJoinPoolWithRunnable](src/main/java/com/cwn/problem/ForkJoinPoolWithRunnable.java)
[ForkJoinPoolRunnableDifferentMethods](src/main/java/com/cwn/problem/ForkJoinPoolRunnableDifferentMethods.java)
2. invoke - Takes ForkJoinTask and perform the operation and return result synchronously 
[ForkJoinPoolWithCallableSynchronous](src/main/java/com/cwn/problem/ForkJoinPoolWithCallableSynchronous.java)
3. submit - Takes Callable or ForkJoinTask and return the ForkJoinTask which we can use to get the result at later time
when we need the result of task. This is used to get result asynchronously. 
[ForkJoinPoolWithCallableAsynchronous](src/main/java/com/cwn/problem/ForkJoinPoolWithCallableAsynchronous.java)

## ForkJoinPool for dividing the problem when task is running insider pool
Let's see how we can submit a task when task is inside or already running within the ForkJoinPool. For running a task
from inside the pool we take an example; in which we will half the number till it is less than or equal to one 
and when is less than or equal to one we will return 1; and sum up with same number. For this we will divide the task 
into two different task. Let's first write it without ForkJoinPool; and then we will convert into the task and see how
we can submit task to ForkThreadPool.
```text
 public static int divideAndConquerProblem(int number) {
        System.out.println("Number "+number+" is Processing by Thread "+Thread.currentThread());
        if (number <= 1) {
            return 1;
        } else {
            int subtaskNumber = number / 2;
            int result1 = divideAndConquerProblem(subtaskNumber);
            int result2 = divideAndConquerProblem(subtaskNumber);
            return result1 + result2;
        }
}
 public static void main(String[] args) throws InterruptedException {
      System.out.println(divideAndConquerProblem(3)); // 2
      System.out.println(divideAndConquerProblem(6)); // 4
      System.out.println(divideAndConquerProblem(20)); //16
```
Above code will run in main thread only in synchronous way and generate the result like below for number 6.
```text
Number 6 is Processing by Thread Thread[main,5,main]
Number 3 is Processing by Thread Thread[main,5,main]
Number 1 is Processing by Thread Thread[main,5,main]
Number 1 is Processing by Thread Thread[main,5,main]
Number 3 is Processing by Thread Thread[main,5,main]
Number 1 is Processing by Thread Thread[main,5,main]
Number 1 is Processing by Thread Thread[main,5,main]
4
```
Number 6 task is divided into two sub-task of 3 and 3, and they process again for 1 and 1 then
we get the result for them sum up and return to parent call, and we find our output for this 4.

Let's do same thing by using ForkJoinPool and ForkJoinTask.
```text
    public static int divideAndConquerProblem(int number) {
        System.out.println("Number "+number+" is Processing by Thread "+Thread.currentThread());
        if (number <= 1) {
            return 1;
        } else {
            int subtaskNumber = number / 2;
            ForkJoinTask<Integer> task1 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            ForkJoinTask<Integer> task2 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            int result1 = task1.invoke();
            int result2 = task2.invoke();
            return result1 + result2;
        }
    }
    public static void main(String[] args) throws InterruptedException {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            pool.submit(()-> System.out.println(divideAndConquerProblem(6)));
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
    }
``` 
Here when we divide the task into 2 tasks we submit the task to pool using invoke method which will return us the 
result. Invoke call work in synchronous way, so it will wait for thread 1 to finished; then it will submit another task. 
Below is output for above program:
```text
Number 6 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 3 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 3 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool.commonPool-worker-9,5,main]
4
```
We can see the task has executed in the pool and when task for 6 is gone into waiting; same thread steals the work for 
task 3, and we can also see that it is process sequentially for result1 then result 2; But What if want to execute the 
tasks asynchronous manner.

To perform the same task in asynchronous manner we are going to fork and join method. fork method return the 
ForkJoinTask; for method submits the task to pool and execute the further instructions and, we can get the result 
after task completed using join method or join will wait for task to complete and move ahead. Lets see this with code.
```text
    public static int divideAndConquerProblem(int number) {
        System.out.println("Number "+number+" is Processing by Thread "+Thread.currentThread());
        if (number <= 1) {
            return 1;
        } else {
            int subtaskNumber = number / 2;

            ForkJoinTask<Integer> task1 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            ForkJoinTask<Integer> task2 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));

            ForkJoinTask<Integer> result1 = task1.fork();
            ForkJoinTask<Integer> result2 = task2.fork();
            return result1.join() + result2.join();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(50);
        pool.submit(()-> System.out.println(divideAndConquerProblem(6)));
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
```
Here task1.fork() will submit the task to pool and then task2.fork() will submit task to pool, and they run in parallel 
then we are joining the result of the these two task when they complete and return the result. Let's see output of above
program.
```text
Number 6 is Processing by Thread Thread[ForkJoinPool-1-worker-57,5,main]
Number 3 is Processing by Thread Thread[ForkJoinPool-1-worker-43,5,main]
Number 3 is Processing by Thread Thread[ForkJoinPool-1-worker-50,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool-1-worker-57,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool-1-worker-57,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool-1-worker-29,5,main]
Number 1 is Processing by Thread Thread[ForkJoinPool-1-worker-36,5,main]
4
```
We can see in above output that Number 3 tasks run in parallel and different threads. 

## RecursiveAction and RecursiveTask
Let's understand first why we need RecursiveAction or RecursiveTask; as we are working with ForkJoinPool and able
to divide the task outside the pool as well as inside the pool. Consider the scenarios where you have to divide the task
which need some data to process, and you use class methods to perform the operation; So like Runnable and Callable
we need RecursiveAction and RecursiveTask to extend at class level; which will help to divide the task within the
ForkJoinPool. Both Class have one abstract method compute. RecursiveAction does not return any result like Runnable 
while RecursiveTask return the result after completing of operation. Let's see an example of Both.
1. RecursiveAction [ForkJoinPoolRecursiveAction](src/main/java/com/cwn/problem/ForkJoinPoolRecursiveAction.java)
2. RecursiveTask [ForkJoinPoolRecursiveTask](src/main/java/com/cwn/problem/ForkJoinPoolRecursiveTask.java)

In RecursiveAction we divided the problem and submit using invokeAll method; and in RecursiveTask we fork the task
and join for the result at the end.

## Let's solve our original problem
We had started with problem with Executor service and count the prime number in range 
[ExecutorServiceInducedDeadLock](src/main/java/com/cwn/problem/ExecutorServiceInducedDeadLock.java).
Lets solve it using the ForkJoinPool and concept we have learned till now.

First we replace the ExecutorService with the ForkJoinPool and create ForkJoinPool in main method. The next action is 
invoke the task for splitAndCompute. Then we will create task in splitAndCompute for splitAndCompute and fork them 
from the task and join. Here is solution after change 
[ExecutorServiceInducedDeadLockSolvedUsingForkJoinPool](src/main/java/com/cwn/problem/ExecutorServiceInducedDeadLockSolvedUsingForkJoinPool.java).

So this will solve the problem of Pool induced lock.
