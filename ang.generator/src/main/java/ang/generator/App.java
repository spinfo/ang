package ang.generator;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException, IOException
    {

//    	Generator g = new Generator();
//    	g.generateList();
//        g.generateSufPrae();
//    	
    	ToText t = new ToText();
    	t.writeToText("anglizismenSuf");
        t.writeToText("anglizismenPrae");
    	
    }
}
