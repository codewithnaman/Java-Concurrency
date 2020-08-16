# Concurrent Programming
Java threads are most common topic which is asked more during interviews or technical discussions, and considered as most
hard to answer. This post will go in two phases. First phase contains the very basic knowledge of threading including some
key terms of the threading world like Race condition, synchronization, volatility, visibility, false sharing and
happens-before. Second phase contains the introduction to concurrent framework of Java introduce in Java 7. Also we 
will cover some preventive and detection algorithm with concurrent programming design patterns. Let's get started.

## Concurrency Basics
**1. Race Condition :** Race condition is condition when two threads want to access same data for reading or writing. 
That will cause in incorrect data read or write. (RaceConditionExample.java does not ensure fix output because
they can execute at same time and value written to variable after execution can be differ.)
```java
public class Singleton{
    private Singleton instance;
    
    private Singleton(){}
    
    public SingleTon getInstance(){
        if(instance==null){
            instance = new Singleton();
        }
        return instance;
    }
}
```

If in above code block two thread will execute the getInstance() method, and if one thread T1 enters into if block and 
go into then pauses execution and gives handle to T2. Now T2 run enters into the getInstance() block checks for condition
that instance is null, which is true because T1 not initialized the object. Now T2 initialize the object and returns
from the method and gives hand to T1. Now since T1 is already checked the condition before giving hand, so now it will
create the object and replace the object created by the T2, which leads to create two objects of Singleton class.

To solve above problem we do synchronization in our code. The synchronization is perform at three levels :
1. Class level (Using static method or by synchronizing Class object instance of particular class)
2. Instance level (Using instance method or by synchronizing this instance)
3. Object or third-instance level (By creating a variable and performing synchronization on it)


When we wanna know which method runs in parallel or which are not. Visualize the lock acquiring process.
(RaceConditionWithSynchronization.java)

**_A common mistake while visualizing this people do that they mix Class level lock with Instance lock.
So to clear that here is thumb rule, every instance have their own lock and there is only
one Class level instance lock. Both can be acquired at same time. So you can execute static method and instance
 method at same time if instance method does not acquired class level already._**
 
 **2. Reentrant Lock :** The lock is called reentrant, When a thread holds a lock, it can enter a block synchronized 
 on the lock it is holding. Ex. If a executing method holds class level lock from a instance and 
 call other method different instance which require again class level lock. Since, The method already have the class lock
 it will not wait for the lock or try to get lock again, it will execute the method and returns.
 
 **3. DeadLock :** A deadlock is a situation where a thread T1 holds a key needed by a thread T2, 
 and T2 holds the key needed by T1. (DeadlockExample.java)
 
 The JVM is able to detect deadlock situations, and can log information to help debug the application.
 But there is not much we can do if a deadlock situation occurs, beside rebooting the JVM.
