package com.doplgangr.secrecy;

/**
 * Created by Haya on 10/25/2016.
 */
        import java.math.BigDecimal;
        import java.math.RoundingMode;
        import java.security.Key;
        import java.security.NoSuchAlgorithmException;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;
        import java.util.Random;
        import javax.crypto.KeyGenerator;
        import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
        import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;
public class Fuzzy_Vault {
  //  public static double[]   minutiaPoints ={-1.0, -0.9, -0.8,-0.7,-0.6,-0.5,-0.4,-0.3,-0.2,-0.1,0,0.1,0.2,0.3,0.4,0.5};// {-0.36815, -0.41932, -0.38212,-0.40143, -0.39030, -0.40935, -0.44100,0, 0.46714, 0.42974, 0.34655, 0.52389, 0.34377, 0.38094, 0.50643, 0.59296};
    public static List<Double> minutia = new ArrayList<Double>();
    public static double[] coefficient= new double [16];
 //public static double[]  polynomial_values = new double [16];
 public static List<Double> polynomial_values  = new ArrayList<Double>();
    public static String image=null;
    public static PolynomialFunction polynomial;
    
    public static Key createKey()
    {
        KeyGenerator keyGenerator;
        Key encryptionKey=null;

        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            encryptionKey = keyGenerator.generateKey();
            System.out.println();
            System.out.println("Key to Encode: ");
            System.out.println(" 128 AES Key in bytes: "+ Arrays.toString(encryptionKey.getEncoded()));
            //convert key byte to double and save it in coefficient arraylist
            for (int y=0; y<encryptionKey.getEncoded().length;y++)
                coefficient [y]=encryptionKey.getEncoded()[y];
            System.out.println("128 AES Key in double: "+Arrays.toString(coefficient));

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return encryptionKey;

    }
    public static void encoding()
    {
      //  for(int i=0; i<minutiaPoints.length;i++)
        //    minutia.add(minutiaPoints[i]);

        //create polynomial, set the key as coefficient
        polynomial = new PolynomialFunction(coefficient);
        System.out.println("-------------------Encoding------------------ \nPolynomial degree: "+polynomial.degree());
        System.out.println("Polynomial: "+polynomial);
        System.out.println("Coffecients: ");
        System.out.println(Arrays.toString(polynomial.getCoefficients()));
        System.out.println("minutia points: ");
        System.out.println(minutia.toString());
        // call evaluate polynomial method
        evaluatePolynomial(polynomial);
        // Add chaff point
        addChaffPoints();
    }
    public static void evaluatePolynomial ( PolynomialFunction pf)
    {
        /**********************************************/
        //This method evaluate minutia point on polynomial, then save the result
        // of the evaluation
        /*********************************************/

        for (int i = 0; i < minutia.size(); i++){
           // polynomial_values [i]= pf.value(minutia[i]);
            polynomial_values.add(i,pf.value(minutia.get(i)));
            System.out.print("polynomial_values: "+polynomial_values.get(i)+", ");
        }

        // save (polynomial_values [i], minutia[i]) and polynomial degree
    }

    public static void addChaffPoints()
    {
        double chaff=0, chaff_value=0;
        Random random = new Random();
        double largest = minutia.get(0);
        for(int j=1; j< minutia.size(); j++)  //get largest number in minutia list
        {   if(minutia.get(j) > largest)
                largest = minutia.get(j);
        }
        //then use the largest to add chaff points that are above the largest
        for (int i=0; i<20;i++) {
            chaff = random.nextInt(90000 - (int) largest + 1) + largest;
            chaff_value = chaff + random.nextDouble();
            if (polynomial.value(chaff) != chaff_value) {
                minutia.add(chaff);
                polynomial_values.add(chaff_value);
            } else
                i--;
        }
       shuffleArrayList();
    }
    private static void shuffleArrayList()
    {
        int index;
        double temp1, temp2;
        Random random = new Random();
        for (int i = minutia.size()-1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp1 = minutia.get(index);
            temp2=polynomial_values.get(index);
            minutia.set(index, minutia.get(i));
            minutia.set(i, temp1);
            polynomial_values.set(index, polynomial_values.get(i));
            polynomial_values.set(i, temp2);
        }
    }


    public static double [] decoding (double []q_minutia, double [] q_polynomial_values )
    {
        // We should get the saved polynomial_values
        PolynomialFunctionLagrangeForm LF = new PolynomialFunctionLagrangeForm(q_minutia,q_polynomial_values);
        System.out.print("-------------------Decoding------------------ \nDegree of Lagrange polynomial:");
        System.out.println(LF.degree());
        System.out.println("Polynomial: "+LF);
        System.out.println("Coffecient of Lagrange polynomial:");
        //   System.out.println(Arrays.toString(LF.getCoefficients()));
        printArray(LF.getCoefficients());
        return coefficient;
        // round issue
        // return LF.getCoefficients();
    }
    public static void printArray (double [] x)
    {
        for (int i =0; i< x.length; i++) {
            System.out.print(round(x[i], 1) + ", ");
            coefficient [i]=round(x[i], 1);
        }
        System.out.println();

    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
