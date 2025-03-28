import java.io.Serializable;
public class Result implements Serializable {
    public long number;
    public long factor1;
    public long factor2;
    public boolean isPrime;
    public Result(long number, long factor1, boolean isPrime) {
        this.number = number;
        if (!isPrime)
        {
            this.factor1 = factor1;
            this.factor2 = number / factor1;
        }
        this.isPrime = isPrime;
    }
}
