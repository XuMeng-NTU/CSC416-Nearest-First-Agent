/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld;

import sim.engine.SimState;
import tileworld.agent.TWAgent;
import tileworld.agent.enhanced.DerivedConstants;
import tileworld.environment.TWEnvironment;

/**
 * TileworldMain
 *
 * @author michaellees
 * Created: Apr 19, 2010
 *
 * Copyright michaellees 2010
 *
 * Description:
 *
 * This is currently not used, but provides ways to run headless (non-gui) 
 * versions of tileworld, including parameter sweeps.
 * 
 */
public class TileworldMain {

    public static final int ITERATION = 100;
    
    public static void main(String args[]){
        
        DerivedConstants.printConstants();
        
        int sum = 0;
        
        boolean showProgress = ITERATION==1;
        
        for(int i=1;i<=ITERATION;i++){
            try{
                int result = doOneSimulation(showProgress);
                System.out.println(result);
                sum+=result; 
            } catch(Exception ex){
                i--;
            }
        }
        
        System.out.println((double)sum/ITERATION);
        
        System.exit(0);
    }
    
    public static int doOneSimulation(boolean showProgress) {

        int result = 0;
        
        TWEnvironment tw = new TWEnvironment(Parameters.seed);
        tw.start();

        long steps = 0;

        while (steps < Parameters.endTime) {    
            if (!tw.schedule.step(tw)) {
                break;
            }
            steps = tw.schedule.getSteps();
            if(showProgress){
                if(steps%500==0){
                    System.out.println(steps+" completed");
                }
            }
        }
        
        for(TWAgent agent : tw.getAgents()){
            result += agent.getScore();
        }
        
        tw.finish();
        
        return result;
    }

    /**
     * Main method for restoring form checkpoints (see Tutorial 2 from Mason docs)
     * @param args
     */
    public static void main2(String[] args) {
        TWEnvironment tw = null;

        // should we load from checkpoint?  I wrote this little chunk of code to
        // check for this to give you the general idea.

        for (int x = 0; x < args.length - 1; x++) // "-checkpoint" can't be the last string
        {
            if (args[x].equals("-checkpoint")) {
                SimState state = SimState.readFromCheckpoint(new java.io.File(args[x + 1]));
                if (state == null) // there was an error -- it got printed out to the screen, so just quit
                {
                    System.exit(1);
                } else if (!(state instanceof TWEnvironment)) // uh oh, wrong simulation stored in the file!
                {
                    System.out.println("Checkpoint contains some other simulation: " + state);
                    System.exit(1);
                } else // we're ready to lock and load!
                {
                    tw = (TWEnvironment) state;
                }
            }
        }

        // ...or should we start fresh?
        if (tw == null) // no checkpoint file requested
        {
            tw = new TWEnvironment();
            tw.start();
        }

        long steps = 0;
        while (steps < 5000) {
            if (!tw.schedule.step(tw)) {
                break;
            }
            steps = tw.schedule.getSteps();
            if (steps % 500 == 0) {
                System.out.println("Steps: " + steps + " Time: " + tw.schedule.time());
                String s = steps + ".TWEnvironment.checkpoint";
                System.out.println("Checkpointing to file: " + s);
                tw.writeToCheckpoint(new java.io.File(s));
            }
        }
        tw.finish();
        System.exit(0);  // make sure any threads finish up
    }

    /**
     * Use the standard doLoop created in SimState, see Tutorial 2 for more details.
     * This method takes some standard argument format for non GUI runs...
     *
     * Format:           java tileworld.Tileworld \
    [-help] [-checkpoint C] [-repeat R] [-seed S] \
    [-for F] [-until U] [-time T] [-docheckpoint D]

    -help             Shows this message.

    -repeat R         Long value > 0: Runs the job R times. The random seed for
    each job is the provided -seed plus the job# (starting at 0).
    Default: runs once only: job number is 0.

    -checkpoint C     String: loads the simulation from file C for
    job# 0. Further jobs are started new using -seed as normal.
    Default: starts a new simulation rather than loading one.

    -until U          Double value >= 0: the simulation must stop when the
    simulation time U has been reached or exceeded.
    Default: don't stop.

    -for N            Long value >= 0: the simulation must stop when N
    simulation steps have transpired.
    Default: don't stop.

    -seed S           Long value not 0: the random number generator seed.
    Default: the system time in milliseconds.

    -time T           Long value >= 0: print a timestamp every T simulation steps.
    If 0, nothing is printed.
    Default: auto-chooses number of steps based on how many
    appear to fit in one second of wall clock time.  Rounds to
    one of 1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, etc.

    -docheckpoint D   Long value > 0: checkpoint every D simulation steps.
    Default: never.
    Checkpoints files named
    <steps>.<job#>.Tutorial1.checkpoint
     * @param args
     */
    public static void main3(String[] args) {
        SimState.doLoop(TWEnvironment.class, args);
        System.exit(0);
    }
}
