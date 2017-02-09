package carleton.comps.javaparser.examples;

public class HelloWorld {
   public static void main(String[] args) { 
      //System.out.println("Hello, World");
      dumb d = new dumb();
      System.out.println(dumb.i);
   }

   public static class dumb {
      public static int i = 1;
   }
}

