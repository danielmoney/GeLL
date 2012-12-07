package Maths;

public interface Real/*<Real>*/
{
    public Real multiply(Real o);   
    public Real multiply(double o);
    public Real add(Real o);  
    public Real add(double o);
    public double ln();   
    public double ln1m();
    public boolean greaterThan(Real o);
    public boolean greaterThan(double o);
    public Real subtract(Real o);
    public Real subtract(double o);
    public Real negate();
    public Real inverse();  
    public Real divide(Real o);
    public Real divide(double o);
    public double toDouble();
    public SmallDouble toSmallDouble();    
        
    public enum RealType
    {
        STANDARD_DOUBLE,
        SMALL_DOUBLE
    }
}
