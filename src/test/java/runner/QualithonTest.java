package runner;

import function.Bot;
import org.junit.jupiter.api.Test;

public class QualithonTest {

    @Test
    public void runTest() throws Exception{
        Bot run = new Bot();
        run.solvePuzzle();
        System.out.println("Done");
    }

}
