Skip to content
 
Search or jump to…

Pull requests
Issues
Marketplace
Explore
 
@thejender8795 
0
0 0 srivasthav8/wumpus
 Code  Issues 0  Pull requests 0  Projects 0  Wiki  Security  Insights
wumpus/MyAgent.java
@srivasthav8 srivasthav8 file is added
9aa2edb on Feb 7, 2018
588 lines (522 sloc)  13 KB
    
package wumpusworld;

/**
 * Contains starting code for creating your own Wumpus World agent. Currently
 * the agent only make a random decision each turn.
 * 
 * @author Johan Hagelback
 */
public class MyAgent implements Agent
{
    private World w;
    int rnd;

    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world
     *            Current world state
     */
    public MyAgent(World world)
    {
	w = world;

	try
	{
	    MyLearningAlgorithm.LearnForWorld();
	}
	catch (Exception ex)
	{
	    System.out.println("Failed to train the algorithm. Will be using random moves");
	}
    }

    /**
     * Asks your solver agent to execute an action.
     */
    public void doAction()
    {
	// Location of the player
	int cX = w.getPlayerX();
	int cY = w.getPlayerY();

	// Basic action:
	// Grab Gold if we can.
	if (w.hasGlitter(cX, cY))
	{
	    w.doAction(World.A_GRAB);
	    return;
	}

	// Basic action:
	// We are in a pit. Climb up.
	if (w.isInPit())
	{
	    w.doAction(World.A_CLIMB);
	    return;
	}

	try
	{
	    int curMove = MyLearningAlgorithm.GetNextMove();

	    // Enum holding the possible actions
	    // private enum Action
	    // {
	    // TurnLeft, TurnRight, Shoot, MoveForward, Grab
	    // }

	    if (curMove == 0)
	    {
		w.doAction(World.A_TURN_LEFT);
	    }
	    else if (curMove == 1)
	    {
		w.doAction(World.A_TURN_RIGHT);
	    }
	    else if (curMove == 2)
	    {
		w.doAction(World.A_SHOOT);
	    }
	    else if (curMove == 3)
	    {
		w.doAction(World.A_MOVE);
	    }
	    else
	    {
		w.doAction(World.A_GRAB);
	    }
	    return;
	}
	catch (Exception ex)
	{
	    // do nothing. let random algo run
	}

	// Test the environment
	if (w.hasBreeze(cX, cY))
	{
	    System.out.println("I am in a Breeze");
	}
	if (w.hasStench(cX, cY))
	{
	    System.out.println("I am in a Stench");
	}
	if (w.hasPit(cX, cY))
	{
	    System.out.println("I am in a Pit");
	}
	if (w.getDirection() == World.DIR_RIGHT)
	{
	    System.out.println("I am facing Right");
	}
	if (w.getDirection() == World.DIR_LEFT)
	{
	    System.out.println("I am facing Left");
	}
	if (w.getDirection() == World.DIR_UP)
	{
	    System.out.println("I am facing Up");
	}
	if (w.getDirection() == World.DIR_DOWN)
	{
	    System.out.println("I am facing Down");
	}

	// decide next move
	rnd = decideRandomMove();
	if (rnd == 0)
	{
	    w.doAction(World.A_TURN_LEFT);
	    w.doAction(World.A_MOVE);
	}

	if (rnd == 1)
	{
	    w.doAction(World.A_MOVE);
	}

	if (rnd == 2)
	{
	    w.doAction(World.A_TURN_LEFT);
	    w.doAction(World.A_TURN_LEFT);
	    w.doAction(World.A_MOVE);
	}

	if (rnd == 3)
	{
	    w.doAction(World.A_TURN_RIGHT);
	    w.doAction(World.A_MOVE);
	}
    }

    /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
	return (int) (Math.random() * 4);
    }

    /**
     * Takes the world as input, learns how to solve the maze using basic
     * Q-learning approach. It learns the utility values for all possible
     * <state, action> tuples.
     * 
     * Each state consists of the following information: 0. Has Wumpus/Gold (2
     * bits) 1. Position (16 possibilities, 4 bits) 2. Direction of player (4
     * possibilities, 2 bits)
     * 
     * State is represented as an integer. (8 useful bits) <2 wumpus/gold
     * bits><4 position bits><2 direction bits>
     * 
     * Possible actions: 0/1. Turn Left/Right (90 degrees) 2. Shoot 3. Move
     * forward 4. Grab
     * 
     * Possible actions are represented as an Enum
     */
    static class MyLearningAlgorithm
    {
	public static boolean TrainingSuccessful = false;

	// variable to maintain current state while using the solution
	// both wumpus and gold are present in grid (11)
	// position is 0 in grid (0000)
	// facing right (10) according to enum
	// 11000010 base 2 = base 10
	private static int curState = 194;

	// assigned by code that uses learning algorithm
	private static WorldMap _myWorld;

	// array of utility values (states X actions)
	private static double _qValue[][] = new double[256][5];
	private static int _reward[] = new int[256];
	private static double _learningRate = 0.8;
	private static double _discountFactor = 0.4;

	// Learning algorithm
	// first initializes the utility array with utilities for success and
	// failure states.
	public static void LearnForWorld() throws Exception
	{
	    MapReader myMapReader = new MapReader();

	    _myWorld = myMapReader.readMaps().get(0);//the training is being done on first map in maps.txt and you have to replace the first map in maps.txt in order to train new maps. We feel there is fault in your map reader.

	    _initializeIsValid();
	    _initializeRewardValues();
	    _initializeQValues();

	    int newState;
	    Action curAction;

	    // train until desirable state is reached

	    for (int iterCount = 0; iterCount <= 100; iterCount++)
	    {
		// iterate over all states and actions
		for (int i = 0; i < 256; i++)
		{
		    // do not run algo on final states
		    if (_isFinalState(i))
		    {
			continue;
		    }

		    for (int j = 0; j < Action.values().length; j++)
		    {
			curAction = Action.values()[j];

			if (_isValidAction(i, curAction))
			{
			    newState = _getStatePostAction(i, curAction);
			    _qValue[i][j] = _updatedQValue(i, newState, j);
			}
			else
			{
			    // set to a low value for invalid moves
			    _qValue[i][j] = -2000;
			}
		    }
		}
	    }

	    TrainingSuccessful = true;
	}

	// find best action and return
	// keep current state variable updated
	public static int GetNextMove() throws Exception
	{
	    if (!TrainingSuccessful)
	    {
		throw new Exception(
				"Failed to train the model. Use random generator");
	    }

	    double maxQValue = -30000;
	    int maxValueAction = -1;
	    for (int i = 0; i < Action.values().length; i++)
	    {
		if (_qValue[curState][i] > maxQValue)
		{
		    maxQValue = _qValue[curState][i];
		    maxValueAction = i;
		}
	    }

	    // update current state before return
	    curState = _getStatePostAction(curState,
			    Action.values()[maxValueAction]);
	    return maxValueAction;
	}

	// private helper variables
	private static boolean _isMoveValid[][] = new boolean[16][4];
	private static int _positionOffset = 2;
	private static int _positionMask = 15;
	private static int _directionOffset = 0;
	private static int _directionMask = 3;
	private static int _wumpusBitMask = 0x40; // (7th bit is 1)
	private static int _goldBitMask = 0x80; // (8th bit is 1)

	private static boolean _isFinalState(int curState)
	{
	    int curPos = (curState >> _positionOffset) & _positionMask;

	    // if wumpus isn't dead
	    if (_myWorld.hasWumpus(curPos % 4 + 1, curPos / 4 + 1)
			    && ((curState & _wumpusBitMask) != 0))
	    {
		return true;
	    }

	    // if gold is taken, then it's an end state
	    if (_myWorld.hasGlitter(curPos % 4 + 1, curPos / 4 + 1)
			    && (curState & _goldBitMask) == 0)
	    {
		return true;
	    }

	    return false;
	}

	private static double _getMaxQForState(int curState)
	{
	    double result = -300000;

	    for (int i = 0; i < Action.values().length; i++)
	    {
		if (_qValue[curState][i] > result)
		{
		    result = _qValue[curState][i];
		}
	    }

	    return result;
	}

	/**
	 * Using the following formula
	 * 
	 * Q(s, a) = Q(s, a) + learningRate * [reward(s') + discountFactor *
	 * (max_{a} Q(s', a)) - Q(s, a)]
	 * 
	 * Using algorithm from: https://en.wikipedia.org/wiki/Q-learning
	 */
	private static double _updatedQValue(int curState, int newState,
			int curAct)
	{
	    double result = 0;

	    result = _reward[newState];
	    result += _getMaxQForState(newState) * _discountFactor;
	    result -= _qValue[curState][curAct];
	    result *= _learningRate;
	    result += _qValue[curState][curAct];

	    return result;
	}

	private static void _initializeQValues()
	{
	    // sets this value to reward value for final states (glitter/death)
	    // -10000 for invalid actions
	    for (int i = 0; i < 256; i++)
	    {
		for (int j = 0; j < Action.values().length; j++)
		{
		    if (_isFinalState(i))
		    {
			_qValue[i][j] = _reward[i];
		    }
		    else
		    {
			_qValue[i][j] = 0;
		    }
		}
	    }
	}

	private static void _initializeRewardValues()
	{
	    int curPos;
	    for (int i = 0; i < 256; i++)
	    {
		curPos = (i >> _positionOffset) & _positionMask;
		_reward[i] = -1;

		// if wumpus is still alive, set it to very low
		if ((i & _wumpusBitMask) != 0)
		{
		    if (_myWorld.hasWumpus(curPos % 4 + 1, curPos / 4 + 1))
		    {
			_reward[i] -= 10000;
		    }
		}
		// if gold is not there, it means gold is taken
		// we should add high reward for it but not so high
		// that wumpus is ignored. so, let's keep it lesser than damage
		// due to wumpus. that way we can end up shooting wumpus
		if ((i & _goldBitMask) == 0)
		{
		    if (_myWorld.hasGlitter(curPos % 4 + 1, curPos / 4 + 1))
		    {
			_reward[i] += 5000;
		    }
		}
		// normal reward logic
		if (_myWorld.hasPit(curPos % 4 + 1, curPos / 4 + 1))
		{
		    _reward[i] -= 1000;
		}
	    }
	}

	private static int _getNewPosition(int curPos, Direction curDir,
			Action curAct) throws Exception
	{
	    if (curAct != Action.MoveForward)
	    {
		return curPos;
	    }

	    if (!_isMoveValid[curPos][curDir.ordinal()])
	    {
		throw new Exception("Invalid move for current position");
	    }

	    switch (curDir)
	    {
	    case Up:
		return (curPos + 4);
	    case Down:
		return (curPos - 4);
	    case Left:
		return (curPos - 1);
	    case Right:
		return (curPos + 1);
	    }

	    return curPos;
	}

	private static Direction _getNewDirection(Direction curDir,
			Action curAct)
	{
	    switch (curAct)
	    {
	    case TurnLeft:
		switch (curDir)
		{
		case Down:
		    return Direction.Right;
		case Up:
		    return Direction.Left;
		case Left:
		    return Direction.Down;
		case Right:
		    return Direction.Up;
		}
	    case TurnRight:
		switch (curDir)
		{
		case Down:
		    return Direction.Left;
		case Up:
		    return Direction.Right;
		case Left:
		    return Direction.Up;
		case Right:
		    return Direction.Down;
		}
	    default:
		return curDir;
	    }
	}

	// a move is valid as long as we are not hitting a wall
	private static boolean _isValidAction(int curState, Action action)
	{
	    int curPos = (curState >> _positionOffset) & _positionMask;
	    Direction curDir = Direction.values()[((curState >> _directionOffset) & _directionMask)];

	    if (action == Action.MoveForward
			    && !_isMoveValid[curPos][curDir.ordinal()])
	    {
		return false;
	    }

	    return true;
	}

	private static int _getStatePostAction(int curState, Action action)
			throws Exception
	{
	    int newState = 0;
	    int curPos = (curState >> _positionOffset) & _positionMask;
	    Direction curDir = Direction.values()[((curState >> _directionOffset) & _directionMask)];

	    int newPos = _getNewPosition(curPos, curDir, action);
	    Direction newDir = _getNewDirection(curDir, action);

	    newState = newPos << 2;
	    newState |= newDir.ordinal();

	    // copy wumpus life's state as it is if not shoot
	    if (action != Action.Shoot)
	    {
		newState |= (curState & _wumpusBitMask);
	    }
	    else
	    {
		// only if shooting is possible
		if (_isValidAction(curState, Action.MoveForward))
		{
		    int myNewPosForShoot = _getNewPosition(curPos, curDir,
				    Action.MoveForward);
		    // check if we are killing wumpus and update
		    // we are checking new pos because of the assumption that we
		    // are
		    // shooting in the direction we are looking in
		    if (_myWorld.hasWumpus(myNewPosForShoot % 4 + 1,
				    myNewPosForShoot / 4 + 1))
		    {
			newState |= 0;
		    }
		    else
		    {
			newState |= (curState & _wumpusBitMask);
		    }
		}
		else
		{
		    newState |= (curState & _wumpusBitMask);
		}
	    }

	    // copy gold state if we aren't grabbing it
	    if (action != Action.Grab)
	    {
		newState |= (curState & _goldBitMask);
	    }
	    else
	    {
		// we grab the gold in current pos. So, checking curPos
		if (_myWorld.hasGlitter(curPos % 4 + 1, curPos / 4 + 1))
		{
		    newState |= 0;
		}
		else
		{
		    newState |= (curState & _goldBitMask);
		}
	    }

	    return newState;
	}

	// Enum holding the possible actions
	private enum Action
	{
	    TurnLeft, TurnRight, Shoot, MoveForward, Grab
	}

	private enum Direction
	{
	    Left, Up, Right, Down
	}

	public static void _initializeIsValid()
	{
	    // initializes all helper variables

	    // initialize valid moves
	    for (int i = 0; i < 16; i++)
	    {
		for (int j = 0; j < 4; j++)
		{
		    // initialize to true and modify later
		    _isMoveValid[i][j] = true;

		    // cannot go up from top row
		    if ((i / 4) == 0 && (j == Direction.Down.ordinal()))
		    {
			_isMoveValid[i][j] = false;
		    }
		    else if ((i / 4) == 3 && (j == Direction.Up.ordinal()))
		    {
			_isMoveValid[i][j] = false;
		    }

		    // cannot go left from left most column OR right from right
		    // most
		    if ((i % 4) == 0 && (j == Direction.Left.ordinal()))
		    {
			_isMoveValid[i][j] = false;
		    }
		    else if ((i % 4) == 3 && (j == Direction.Right.ordinal()))
		    {
			_isMoveValid[i][j] = false;
		    }
		}
	    }
	}
    }
}
© 2019 GitHub, Inc.
Terms
Privacy
Security
Status
Help
Contact GitHub
Pricing
API
Training
Blog
About
