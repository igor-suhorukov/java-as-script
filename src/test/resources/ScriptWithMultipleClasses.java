package mypackage;

import java.util.stream.IntStream;

class T1 implements MyInterface{

}

class T2 extends T1{
    
    public static long getSum(){
        return IntStream.of(1,2,3,4,5).asLongStream().sum();
    }

    @Override
    public void run() {
        System.out.println(getSum());
    }
}

interface MyInterface extends Runnable{
    default void run(){}
}
