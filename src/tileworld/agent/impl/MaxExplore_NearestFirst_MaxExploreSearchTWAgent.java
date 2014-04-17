/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent.impl;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;
import tileworld.Parameters;
import tileworld.agent.TWAction;
import tileworld.agent.TWAgent;
import tileworld.agent.TWThought;
import tileworld.agent.enhanced.DerivedConstants;
import tileworld.agent.enhanced.ProbabilityCalculator;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;
import tileworld.planners.enhanced.TravelPath;
import tileworld.planners.enhanced.TravelPathGenerator;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class MaxExplore_NearestFirst_MaxExploreSearchTWAgent extends TWAgent{
    
    private TravelPathGenerator planner;
    private TWEntity target;
    
    public MaxExplore_NearestFirst_MaxExploreSearchTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        
        planner = new TravelPathGenerator(this.memory);
    }

    protected TWThought think() {
        if(this.x==0 && this.y==0 && this.fuelLevel<=Parameters.defaultFuelLevel/2){
            return new TWThought(TWAction.REFUEL, null);
        }

        double urgency = fuelControlUrgency(x, y, fuelLevel);
                
        if(this.fuelLevel<=((x+y)+2*ProbabilityCalculator.inverseProbabilityHavingLessThan(TWObstacle.class, (x+y), 0.75))){
            AstarPathGenerator pather = new AstarPathGenerator(this.getEnvironment(), this, Math.max(Parameters.xDimension, Parameters.yDimension));
            TWPath pathBack = pather.findPath(x, y, 0, 0);

            if (pathBack == null) {
                if(x-1>=0 && y-1>=0){
                    if (Math.random() < 0.5) {
                        return new TWThought(TWAction.MOVE, TWDirection.W);
                    } else {
                        return new TWThought(TWAction.MOVE, TWDirection.N);
                    }
                } else if(x-1>=0){
                    return new TWThought(TWAction.MOVE, TWDirection.W);
                } else if(y-1>=0){
                    return new TWThought(TWAction.MOVE, TWDirection.N);
                }
            } else{
                return new TWThought(TWAction.MOVE, pathBack.getStep(0).getDirection());
            }
        }
        
        if(this.carriedTiles.isEmpty()){
            TWTile tile = this.getMemory().getNearbyTile(x, y, DerivedConstants.AVERAGE_STEP_TO_TILE);
            if(tile==null){
                this.getMemory().nullTarget();
                return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
            } else{
                if(this.sameLocation(tile)){
                    this.target = tile;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PICKUP, null);
                } else{
                    this.getMemory().setTarget(tile.getX(), tile.getY());
                    TravelPath path = planner.findMaxExploredPath(x, y, tile.getX(), tile.getY(), memory.getSimulationTime());
                    if(path!=null){
                        return new TWThought(TWAction.MOVE, pointToDirection(x, y, path.getPath().get(0).x, path.getPath().get(0).y));
                    } else{
                        this.getMemory().nullTarget();
                        return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
                    }
                }
            }
        } else if(this.carriedTiles.size()>=1 && this.carriedTiles.size()<3){
            TWTile tile = this.getMemory().getNearbyTile(x, y, DerivedConstants.AVERAGE_STEP_TO_TILE);
            TWHole hole = this.getMemory().getNearbyHole(x, y, DerivedConstants.AVERAGE_STEP_TO_HOLE);
            
            TravelPath path;
            
            if(tile==null && hole==null){
                this.getMemory().nullTarget();
                return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
            } else if(tile!=null && hole==null){
                if(this.sameLocation(tile)){
                    this.target = tile;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PICKUP, null);
                } else{
                    this.getMemory().setTarget(tile.getX(), tile.getY());
                    path = planner.findMaxExploredPath(x, y, tile.getX(), tile.getY(), memory.getSimulationTime());
                }
                
            } else if(tile==null && hole!=null){
                if(this.sameLocation(hole)){
                    this.target = hole;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                } else{
                    this.getMemory().setTarget(hole.getX(), hole.getY());
                    path = planner.findMaxExploredPath(x, y, hole.getX(), hole.getY(), memory.getSimulationTime());
                }
            } else{
                
                if(this.sameLocation(tile)){
                    this.target = tile;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PICKUP, null);
                } 
                
                if(this.sameLocation(hole)){
                    this.target = hole;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                }
                
                TravelPath tilePath = planner.findMaxExploredPath(x, y, tile.getX(), tile.getY(), memory.getSimulationTime());
                TravelPath holePath = planner.findMaxExploredPath(x, y, hole.getX(), hole.getY(), memory.getSimulationTime());
                
                if(tilePath==null && holePath==null){
                    path = null;
                } else if(tilePath!=null && holePath==null){
                    path = tilePath;
                } else if(tilePath==null && holePath!=null){
                    path = holePath;
                } else{
                    if(tilePath.getPath().size()<holePath.getPath().size()){
                        path = tilePath;
                        this.getMemory().setTarget(tile.getX(), tile.getY());
                    } else{
                        path = holePath;
                        this.getMemory().setTarget(hole.getX(), hole.getY());
                    }
                }
            }
            if (path != null) {
                return new TWThought(TWAction.MOVE, pointToDirection(x, y, path.getPath().get(0).x, path.getPath().get(0).y));
            } else {
                this.getMemory().nullTarget();
                return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
            }
        } else{
            TWHole hole = this.getMemory().getNearbyHole(x, y, DerivedConstants.AVERAGE_STEP_TO_HOLE);
            if(hole==null){
                this.getMemory().nullTarget();
                return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
            } else{
                if(this.sameLocation(hole)){
                    this.target = hole;
                    this.getMemory().nullTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                } else{
                    this.getMemory().setTarget(hole.getX(), hole.getY());
                    TravelPath path = planner.findMaxExploredPath(x, y, hole.getX(), hole.getY(), memory.getSimulationTime());
                    if (path != null) {
                        return new TWThought(TWAction.MOVE, pointToDirection(x, y, path.getPath().get(0).x, path.getPath().get(0).y));
                    } else {
                        this.getMemory().nullTarget();
                        return new TWThought(TWAction.MOVE, getMaxExploreDirection(urgency));
                    }
                }
            }
        }
    }

    @Override
    protected void act(TWThought thought) {
        if(thought.getAction()==TWAction.MOVE){
            try {
                this.move(thought.getDirection());
            }  catch (CellBlockedException ex) {
                List<TWDirection> possibleDirections = new ArrayList();
                if(thought.getDirection()==TWDirection.E || thought.getDirection()==TWDirection.W){
                    if(this.getEnvironment().isInBounds(x, y+1) && !(this.getMemory().getPercept(x, y+1).getO() instanceof TWObstacle) && this.fuelLevel>=(x+y+1)){
                        possibleDirections.add(TWDirection.S);
                    }
                    if(this.getEnvironment().isInBounds(x, y-1) && !(this.getMemory().getPercept(x, y-1).getO() instanceof TWObstacle)){
                        possibleDirections.add(TWDirection.N);
                    }
                } else {
                    if(this.getEnvironment().isInBounds(x+1, y) && !(this.getMemory().getPercept(x+1, y).getO() instanceof TWObstacle) && this.fuelLevel>=(x+y+1)){
                        possibleDirections.add(TWDirection.E);
                    }
                    if(this.getEnvironment().isInBounds(x-1, y) && !(this.getMemory().getPercept(x-1, y).getO() instanceof TWObstacle)){
                        possibleDirections.add(TWDirection.W);
                    }
                }
                
                if(possibleDirections.isEmpty()){
                    this.act(new TWThought(TWAction.MOVE, TWDirection.Z));
                } else{
                    int rand = (int) (Math.random() * possibleDirections.size());
                    TWThought newThought = new TWThought(thought.getAction(), possibleDirections.get(rand));
                    this.act(newThought);
                }
            }
        } else if(thought.getAction()==TWAction.PICKUP){
            this.pickUpTile((TWTile) this.target);
        } else if(thought.getAction()==TWAction.PUTDOWN){
            this.putTileInHole((TWHole) this.target);
        } else if(thought.getAction()==TWAction.REFUEL){
            this.refuel();
        }
    }

    private double fuelControlUrgency(int x, int y, double fuelLevel){
        if(fuelLevel>=(1+DerivedConstants.FUEL_SAFETY_MARGIN) * (x+y)){
            return 1;
        } else{
            double t = (((double)fuelLevel)/(x+y)-1)/DerivedConstants.FUEL_SAFETY_MARGIN;
            return t;
        }
    }
    
    private TWDirection getMaxExploreDirection(double urgency){
        List<TWDirection> iMoves = getIndividualMaxExploreDirection(x,y);

        TWAgent agent = getTheOtherAgent();

        if(agent!=null){

            List<TWDirection> gMoves = getGroupMaxExploreDirection(x,y, agent.getX(), agent.getY(), iMoves);

            if (urgency < 1) {
                if (gMoves.contains(TWDirection.W) && gMoves.contains(TWDirection.N)) {
                    double t = Math.random();
                    if (t < 0.5) {
                        return TWDirection.W;
                    } else {
                        return TWDirection.N;
                    }
                } else if (gMoves.contains(TWDirection.W)) {
                    return TWDirection.W;
                } else if (gMoves.contains(TWDirection.N)) {
                    return TWDirection.N;
                }
            }            

            if (urgency < 1) {
                if (iMoves.contains(TWDirection.W) && iMoves.contains(TWDirection.N)) {
                    double t = Math.random();
                    if (t < 0.5) {
                        return TWDirection.W;
                    } else {
                        return TWDirection.N;
                    }
                } else if (iMoves.contains(TWDirection.W)) {
                    return TWDirection.W;
                } else if (iMoves.contains(TWDirection.N)) {
                    return TWDirection.N;
                }
            }    
            
            int rand = (int) (Math.random()*gMoves.size());
            return gMoves.get(rand);
        } else{

            if (urgency < 1) {
                if (iMoves.contains(TWDirection.W) && iMoves.contains(TWDirection.N)) {
                    double t = Math.random();
                    if (t < 0.5) {
                        return TWDirection.W;
                    } else {
                        return TWDirection.N;
                    }
                } else if (iMoves.contains(TWDirection.W)) {
                    return TWDirection.W;
                } else if (iMoves.contains(TWDirection.N)) {
                    return TWDirection.N;
                }
            }              
            
            int rand = (int) (Math.random()*iMoves.size());
            return iMoves.get(rand);
        }
    }
    
    private List<TWDirection> getGroupMaxExploreDirection(int curX, int curY, int agentX, int agentY, List<TWDirection> directions){
        List<TWDirection> result = new ArrayList();
        for(TWDirection direction : directions){
            
            Int2D nextPos = direction.advance(new Int2D(curX, curY));
            
            int newDist = this.getMemory().distance(nextPos.x, nextPos.y, agentX, agentY);
            int oldDist = this.getMemory().distance(curX, curY, agentX, agentY);
            
            if(newDist < oldDist && (Math.abs(nextPos.x - agentX)>2*Parameters.defaultSensorRange+1 || Math.abs(nextPos.y - agentY)>2*Parameters.defaultSensorRange+1)){
                result.add(direction);
            }
        }
        if(result.isEmpty()){
            return directions;
        } else{
            return result;
        }
    }
    
    private List<TWDirection> getIndividualMaxExploreDirection(int curX, int curY){
        List<TWDirection> moves = new ArrayList();
        
        int maxExplored = 0;
        int explored;
        if(this.getEnvironment().isInBounds(curX+1, curY)){
            explored = this.getMemory().countNewlyExplored(curX+1, curY, curX, curY, memory.getSimulationTime());
            if(explored>maxExplored){
                maxExplored = explored;
                moves = new ArrayList();
                moves.add(TWDirection.E);
            } else if(explored==maxExplored){
                moves.add(TWDirection.E);
            }
        }
        if(this.getEnvironment().isInBounds(curX-1, curY)){
            explored = this.getMemory().countNewlyExplored(curX-1, curY, curX, curY, memory.getSimulationTime());
            if(explored>maxExplored){
                maxExplored = explored;
                moves = new ArrayList();
                moves.add(TWDirection.W);
            } else if(explored==maxExplored){
                moves.add(TWDirection.W);
            }
        }
        if(this.getEnvironment().isInBounds(curX, curY+1)){
            explored = this.getMemory().countNewlyExplored(curX, curY+1, curX, curY, memory.getSimulationTime());
            if(explored>maxExplored){
                maxExplored = explored;
                moves = new ArrayList();
                moves.add(TWDirection.S);
            } else if(explored==maxExplored){
                moves.add(TWDirection.S);
            }        
        }       
        if(this.getEnvironment().isInBounds(curX, curY-1)){
            explored = this.getMemory().countNewlyExplored(curX, curY-1, curX, curY, memory.getSimulationTime());
            if(explored>maxExplored){
                maxExplored = explored;
                moves = new ArrayList();
                moves.add(TWDirection.N);
            } else if(explored==maxExplored){
                moves.add(TWDirection.N);
            } 
        }  
        return moves;
    }
    public TWDirection pointToDirection(int x, int y, int nx, int ny){
        if(nx-x>0 && ny==y){
            return TWDirection.E;
        } else if(nx-x<0 && ny==y){
            return TWDirection.W;
        } else if(nx==x && ny-y>0){
            return TWDirection.S;
        } else if(nx==x && ny-y<0){
            return TWDirection.N;
        } else{
            return TWDirection.Z;
        }
    }
    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }

    @Override
    public String getName() {
        return "Dumb Agent";
    }
    
    private TWAgent getTheOtherAgent(){
        for(TWAgent agent : this.getEnvironment().getAgents()){
            if(agent!=this){
                return agent;
            }
        }
        return null;
    }
    
}
