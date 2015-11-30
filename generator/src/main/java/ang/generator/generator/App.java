package ang.generator.generator;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException, IOException
    {

    	Generator g = new Generator();
        g.generate();
    	
    	ToText t = new ToText();
    	t.writeThisDown("anglizismenSuf");
        t.writeThisDown("anglizismenPrae");
    	
    }
}
