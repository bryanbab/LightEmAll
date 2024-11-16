import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// to represent the LightEmAll world
class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all edges
  ArrayList<Edge> allEdges;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  // for rotations
  Random rand;
  // win state
  boolean win;
  // timer values
  int seconds; 
  int minutes;
  // click value
  int clicks;
  // bias for edges
  // horizontal, vertical, or normal
  String bias;
  // representatives in board
  HashMap<GamePiece, GamePiece> representatives;
  
  // regular game constructor (no bias)
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    // origin: top left
    this.powerRow = 0;
    this.powerCol = 0;
    // initializes board
    this.board = this.makeBoard();
    // to randomize tiles and edge weights
    this.rand = new Random();
    // no for regular game
    this.bias = "normal";
    // assigning edges
    this.allEdges = new ArrayList<Edge>();
    this.assignEdges();
    // setting representatives
    this.representatives = this.setReps();
    // using kruskals to reduce edge list 
    // and get mst
    this.mst = this.kruskal();
    // connecting pieces based off mst
    this.connectPieces();
    // to randomly rotate tiles for gameplay
    this.randomStart();
    // win condition
    this.win = false;
    // BFS
    this.lightEmUp();
    // timer values, start at 0
    this.seconds = 0;
    this.minutes = 0;
    // click value, starts at 0
    this.clicks = 0;
  }
  
  // game constructor for choosing bias
  LightEmAll(int width, int height, String bias) {
    this.width = width;
    this.height = height;
    // origin: top left
    this.powerRow = 0;
    this.powerCol = 0;
    // initializes board
    this.board = this.makeBoard();
    // to randomize tiles and edge weights
    this.rand = new Random();
    // ability to choose bias based off of String input
    // "horizontal", "vertical", or "normal"
    this.bias = bias;
    // assignign edges
    this.allEdges = new ArrayList<Edge>();
    this.assignEdges();
    // setting representatives
    this.representatives = this.setReps();
    // using kruskals to reduce edge list 
    // and get mst
    this.mst = this.kruskal();
    // connects tiles based off of mst
    this.connectPieces();
    // randomly rotates the tiles 
    this.randomStart();
    // win condition
    this.win = false;
    // BFS
    this.lightEmUp();
    // timer values
    // start at 0
    this.seconds = 0;
    this.minutes = 0;
    // click value
    // starts at 0
    this.clicks = 0;
  }
  
  // constructor for testing, takes in a seeded random
  LightEmAll(int width, int height, Random rand) {
    this.width = width;
    this.height = height;
    // always in the middle (part 1)
    this.powerRow = 0;
    this.powerCol = 0;
    // initializes board
    this.board = this.makeBoard();
    // seeded random for testing
    this.rand = rand;
    // no bias for tests
    this.bias = "normal";
    // assigning edges
    this.allEdges = new ArrayList<Edge>();
    this.assignEdges();
    // setting representatives
    this.representatives = this.setReps();
    // using kruskals to reduce edge list 
    // and get mst
    this.mst = this.kruskal();
    // connects tiles based off of mst
    this.connectPieces();
    // randomly rotates the tiles 
    this.randomStart();
    // win condition
    this.win = false;
    // BFS
    this.lightEmUp();
    // timer values
    // start at 0
    this.seconds = 0;
    this.minutes = 0;
    // click value
    // starts at 0
    this.clicks = 0;
  }

  // to randomly rotate tiles at the start of the game
  public void randomStart() {
    // random amount of rotations 
    int rotateIt = rand.nextInt(4);
    for (int w = 0; w < width; w++) {
      for (int h = 0; h < height; h++) {
        GamePiece tile = this.board.get(h).get(w);
        for (int r = 0; r < rotateIt; r++) {
          // rotating tile rotateIt amount of times
          tile.rotate();
        }
        // resetting random amount of rotations for next tile
        rotateIt = rand.nextInt(4);
      }
    }
  }
  
  // to make the board of the game
  public ArrayList<ArrayList<GamePiece>> makeBoard() {
    ArrayList<ArrayList<GamePiece>> board = new ArrayList<ArrayList<GamePiece>>();

    for (int h = 0; h < this.height; h++) {
      // creating rows
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      
      for (int w = 0; w < this.width; w++) {
        // creating each GamePiece in columns
        GamePiece gp = new GamePiece(h, w);
        row.add(gp);
        // setting powerStation to origin (top left)
        if (h == 0 && w == 0) {
          gp.powerStation = true;
        }
      }
      // adding everything to board
      board.add(row);  
    }
    return board;
  }
  
  // to connect the pieces by changing their boolean values
  // based on the edges in the mst
  void connectPieces() {
    for (Edge e : this.mst) {
 
      GamePiece gp1 = e.fromNode;
      GamePiece gp2 = e.toNode;
      
      if (gp1.row < gp2.row && gp1.col == gp2.col) {
        gp1.bottom = true;
        gp2.top = true;
      }
      
      if (gp1.row >  gp2.row && gp1.col == gp2.col) {
        gp1.top = true;
        gp2.bottom = true;
      }
      
      if (gp1.row == gp2.row && gp1.col < gp2.col) {
        gp1.right = true;
        gp2.left = true;
      }
      
      if (gp1.row == gp2.row && gp1.col > gp2.col) {
        gp1.left = true;
        gp2.right = true;
      }
    }
  }
  
  // to draw the current scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    WorldImage clock = new TextImage("【" + this.minutesAsString() + " : " 
        + this.secondsAsString() + "】", 30, FontStyle.BOLD, Color.DARK_GRAY);
    WorldImage clicks =  new TextImage("Rotations: " + 
        Integer.toString(this.clicks), 25, FontStyle.BOLD, Color.DARK_GRAY);
    WorldImage box = new RectangleImage(width * 35, height * 20, 
        OutlineMode.SOLID, Color.LIGHT_GRAY);
    WorldImage winText =  new AboveImage(new TextImage("YOU WIN!", 
        width * 7, FontStyle.BOLD, Color.black), 
        new AboveImage(new TextImage("Clicks: " + Integer.toString(this.clicks), 
            width * 3, FontStyle.BOLD, Color.black), 
            new TextImage("Press r to restart", width * 3, FontStyle.BOLD, Color.black)));
    WorldImage winBox = new OverlayImage(winText, box);
    for (int w = 0; w < width; w++) {
      for (int h = 0; h < height; h++) {
        GamePiece tile = this.board.get(h).get(w);
        // in-play game scene
        if (tile.powerStation) {
          scene.placeImageXY(tile.tileImage(70, 5, Color.red, true), w * 70 + 35, h * 70 + 35);
        }

        else if (tile.powered) {
          scene.placeImageXY(tile.tileImage(70, 5, Color.red, false), w * 70 + 35, h * 70 + 35);
        }

        else {
          scene.placeImageXY(tile.tileImage(70, 5, Color.LIGHT_GRAY, false), w * 70 + 35,
              h * 70 + 35);
        }
      }
    }
    // scene when game is won
    if (this.win) {
      scene.placeImageXY(winBox, width * 35, height * 35);
    }
    // clock and click counts
    scene.placeImageXY(clock, width * 60, height * 70 + 35);
    scene.placeImageXY(clicks, width * 10, height * 70 + 35);
    return scene;
  }
  
  // timer for how long the game has been running
  public void onTick() {
    if (!this.win) {
      if (this.seconds > 0 && this.seconds % 59 == 0) {
        this.seconds = 0;
        this.minutes += 1;
      }
      else {
        this.seconds += 1;
      }
    }
  }

  // to display one digit seconds with a 0 before it
  public String secondsAsString() {
    if (this.seconds < 10) {
      return "0" + Integer.toString(seconds);
    }
    else {
      return Integer.toString(seconds);
    }
  }

  // to display one digit seconds with a 0 before it
  public String minutesAsString() {
    if (this.minutes < 10) {
      return "0" + Integer.toString(minutes);
    }
    else {
      return Integer.toString(minutes);
    }
  }
  
  // to assign random edge values for the GamePieces and mst
  void assignEdges() {
    int val = width * height;
    for (int h = 0; h < this.height; h++) {
      for (int w = 0; w < this.width; w++) {
        GamePiece tile = board.get(h).get(w);
        // no bias, all randomly assigned
        if (bias.equals("normal")) {
          if (h > 0) {
            Edge e = new Edge(tile, board.get(h - 1).get(w), rand.nextInt(val));
            allEdges.add(e);
          }
          if (h < height - 1) {
            Edge e = new Edge(tile, board.get(h + 1).get(w), rand.nextInt(val));
            allEdges.add(e);
          }
          if (w > 0) {
            Edge e = new Edge(tile, board.get(h).get(w - 1), rand.nextInt(val));
            allEdges.add(e);
          }
          if (w < width - 1) {
            Edge e = new Edge(tile, board.get(h).get(w + 1), rand.nextInt(val));
            allEdges.add(e);
          }
        }
        // horizontal bias
        // giving vertical wiring a less edge weight
        if (bias.equals("horizontal")) {
          if (h > 0) {
            Edge e = new Edge(tile, board.get(h - 1).get(w), rand.nextInt(1));
            allEdges.add(e);
          }
          else if (h < height - 1) {
            Edge e = new Edge(tile, board.get(h + 1).get(w), rand.nextInt(1));
            allEdges.add(e);
          }
          if (w > 0) {
            Edge e = new Edge(tile, board.get(h).get(w - 1), rand.nextInt(val));
            allEdges.add(e);
          }
          else if (w < width - 1) {
            Edge e = new Edge(tile, board.get(h).get(w + 1), rand.nextInt(val));
            allEdges.add(e);
          }
        }
        // vertical bias
        // giving vertical wiring a less edge weight
        if (bias.equals("vertical")) {
          if (h > 0) {
            Edge e = new Edge(tile, board.get(h - 1).get(w), rand.nextInt(val));
            allEdges.add(e);
          }
          else if (h < height - 1) {
            Edge e = new Edge(tile, board.get(h + 1).get(w), rand.nextInt(val));
            allEdges.add(e);
          }
          if (w > 0) {
            Edge e = new Edge(tile, board.get(h).get(w - 1), rand.nextInt(1));
            allEdges.add(e);
          }
          else if (w < width - 1) {
            Edge e = new Edge(tile, board.get(h).get(w + 1), rand.nextInt(1));
            allEdges.add(e);
          }
        }
      }
    }
    // sorting from least to greatest edge values
    allEdges.sort(new EdgeSorting());
  }
  
  // to set GamePieces as representatives of themselves
  HashMap<GamePiece, GamePiece> setReps() {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    for (ArrayList<GamePiece> g : board) {
      for (GamePiece p : g) {
        representatives.put(p, p);
      }
    }
    return representatives;
  }
  
  // to find whether two nodes are in the same group
  public GamePiece find(GamePiece node) {
    GamePiece from = this.representatives.get(node);
   
    if (node.equals(from)) {
      return from;
    } 
    else {
      return find(from);
    }
  }

  // to union two disjoint groups together
  void union(GamePiece from, GamePiece to) {
    this.representatives.put(find(from), find(to));
  }

  // implementation of kruskal's algorithm to find the minimum spanning tree for the board
  public ArrayList<Edge> kruskal() {  
    
    ArrayList<Edge> worklist = new ArrayList<Edge>(); // all edges in graph, sorted by edge weights;
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();

    // adding edges to the worklist
    worklist.addAll(allEdges);
    
    // reducing to one tree
    while (worklist.size() > 1) {
      Edge curr = worklist.remove(0);

      if (find(curr.fromNode).equals(find(curr.toNode))) {
       // do nothing  
      }

      // union the two values together
      else { 
        edgesInTree.add(curr);
        union(find(curr.fromNode),
            find(curr.toNode));
      }
    }
    return edgesInTree;
  }

  // handler for mouse clicks clicks
  public void onMouseClicked(Posn pos) {
    int tileSize = 70;
    int w = Math.floorDiv(pos.x, tileSize);
    int h = Math.floorDiv(pos.y, tileSize);

    // constraints for out of bounds and if won
    if (w > this.width - 1 || h > this.height - 1 || this.win) {
      return;
    }
    
    GamePiece tileClicked = board.get(h).get(w);

    tileClicked.rotate();
    
    // updating clicks values for each rotation
    this.clicks += 1;
    
    // running BFS method and checking for win after each click
    this.lightEmUp();
    this.winCondition();
  }

  // keyEvent for moving around powerStation
  public void onKeyEvent(String key) {
    
    // to make the game stop when won
    // only an option to restart
    if (this.win) {
      if (key.equals("r")) {
        // using same bias as before
        String gameBias = this.bias;
        
        this.powerRow = 0;
        this.powerCol = 0;
        this.board = this.makeBoard();
        this.rand = new Random();
        this.bias = gameBias;
        this.allEdges = new ArrayList<Edge>();
        this.assignEdges();
        this.representatives = this.setReps();
        this.mst = this.kruskal();
        this.connectPieces();
        this.randomStart();
        this.win = false;
        this.lightEmUp();
        this.seconds = 0;
        this.minutes = 0;
        this.clicks = 0;
      }
      else {
        return;
      }
    }
    // tile where the power station is
    GamePiece tile = this.board.get(this.powerRow).get(this.powerCol);
    
    // if not leftmost column
    if (this.powerCol > 0) {
      GamePiece tileLeft = this.board.get(this.powerRow).get(this.powerCol - 1);

      if (key.equals("left") && tile.hasLeft() && tileLeft.hasRight()) {
        tile.removeStation();
        int newPowerCol = this.powerCol - 1;
        // tile where station was moved to
        GamePiece newTile = this.board.get(this.powerRow).get(newPowerCol);
        newTile.placeStation();
        this.powerCol = newPowerCol;
        this.lightEmUp();

      }
    }

    // if not rightmost column
    if (this.powerCol < width - 1) {
      GamePiece tileRight = this.board.get(this.powerRow).get(this.powerCol + 1);

      if (key.equals("right") && tile.hasRight() && tileRight.hasLeft()) {
        tile.removeStation();
        int newPowerCol = this.powerCol + 1;
        // tile where station was moved to
        GamePiece newTile = this.board.get(this.powerRow).get(newPowerCol);
        newTile.placeStation();
        this.powerCol = newPowerCol;
        this.lightEmUp();

      }
    }

    // if not top row
    if (this.powerRow > 0) {
      GamePiece tileTop = this.board.get(this.powerRow - 1).get(this.powerCol);

      if (key.equals("up") && tile.hasTop() && tileTop.hasBottom()) {
        tile.removeStation();
        int newPowerRow = this.powerRow - 1;
        // tile where station was moved to
        GamePiece newTile = this.board.get(newPowerRow).get(this.powerCol);
        newTile.placeStation();
        this.powerRow = newPowerRow;
        this.lightEmUp();

      }
    }

    // if not bottom row
    if (this.powerRow < height - 1) {
      GamePiece tileBottom = this.board.get(this.powerRow + 1).get(this.powerCol);

      if (key.equals("down") && tile.hasBottom() && tileBottom.hasTop()) {
        tile.removeStation();
        int newPowerRow = this.powerRow + 1;
        // tile where station was moved to
        GamePiece newTile = this.board.get(newPowerRow).get(this.powerCol);
        newTile.placeStation();
        this.powerRow = newPowerRow;
        this.lightEmUp();
      }
    }
    
  }

  // BFS for powering neighboring pieces
  public void lightEmUp() {
    Queue<GamePiece> worklist = new LinkedList<GamePiece>();
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
    GamePiece station = this.board.get(this.powerRow).get(this.powerCol);
    // initialize worklist with the powerstation
    worklist.add(station);

    for (int w = 0; w < width; w++) {
      for (int h = 0; h < height; h++) {
        GamePiece tile = this.board.get(h).get(w);
        tile.powerOff();
      }
    }

    // as long as the worklist isn't empty
    while (!worklist.isEmpty()) {
      GamePiece next = worklist.poll();
      next.powerOn();

      // we've already seen this one
      if (!alreadySeen.contains(next)) {
        if (next.col > 0) {
          GamePiece tileLeft = this.board.get(next.row).get(next.col - 1);

          if (next.hasLeft() && tileLeft.hasRight()) {
            worklist.add(tileLeft);
          }
        }

        if (next.col < width - 1) {
          GamePiece tileRight = this.board.get(next.row).get(next.col + 1);

          if (tileRight.hasLeft() && next.hasRight()) {
            worklist.add(tileRight);
          }
        }

        if (next.row > 0) {
          GamePiece tileTop = this.board.get(next.row - 1).get(next.col);

          if (tileTop.hasBottom() && next.hasTop()) {
            worklist.add(tileTop);
          }
        }
        
        if (next.row < height - 1) {
          GamePiece tileBottom = this.board.get(next.row + 1).get(next.col);

          if (tileBottom.hasTop() && next.hasBottom()) {
            worklist.add(tileBottom);
          }
        }
        alreadySeen.add(next);
      }
    }
  }

  // to check if the game has been won
  void winCondition() {
    int target = width * height;
    for (int w = 0; w < width; w++) {
      for (int h = 0; h < height; h++) {
        GamePiece tile = this.board.get(h).get(w);
        if (tile.powered) {
          target -= 1;
        }
      }
    }
    if (target == 0) {
      this.win = true;
    }
  }
}

// to represent a GamePiece
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;
  int distance;

  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
    this.distance = 0;
  }
  
  // overriding equals to check equality of GamePieces
  public boolean equals(Object that) {
    if (!(that instanceof GamePiece)) {
      return false;
    }
    else {
      GamePiece other = (GamePiece) that;
      return this.row == other.row && this.col == other.col; 
    } 
  }
  
  //overriding equals for GamePieces
  public int hashCode() {
    return this.col * 5000;
  }

  // Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the
  // power station
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that
    // can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }

  // to rotate this tiles wires
  void rotate() {
    boolean originalLeft = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = this.top;
    this.top = originalLeft;
  }

  // does this title have a bottom?
  boolean hasBottom() {
    return this.bottom;
  }
 
  //does this title have a top?
  boolean hasTop() {
    return this.top;
  }

  //does this title have a left?
  boolean hasLeft() {
    return this.left;
  }

  //does this title have a right?
  boolean hasRight() {
    return this.right;
  }

  // to place a powerStation at this tile
  void placeStation() {
    this.powerStation = true;
  }
  
  // to place remove the powerStation at this tile
  void removeStation() {
    this.powerStation = false;
  }

  // to power on this tile
  void powerOn() {
    this.powered = true;
  }

  //to power off this tile
  void powerOff() {
    this.powered = false;
  }
}

// to represent an edge 
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }
}

//comparator for sorting edges
class EdgeSorting implements Comparator<Edge> {

  // compares the weight of two edges 
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }
}

// examples and tests for the world
class ExamplesLight {
  
  // to run the game
  void testBigBang(Tester t) {
    LightEmAll world = new LightEmAll(7, 7);
    int worldWidth = 70 * world.width;
    int worldHeight = 70 * world.height + 80;
    double tickRate = 1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // 2 x 2 board
  LightEmAll l1;
  GamePiece g0;
  GamePiece g1;
  GamePiece g2;
  GamePiece g3;
  Edge e0;
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  ArrayList<GamePiece> r0;
  ArrayList<GamePiece> r1;
  
  
  // 3 x 2 board
  LightEmAll l2;
  GamePiece g4;
  GamePiece g5;
  GamePiece g6;
  GamePiece g7;
  GamePiece g8;
  GamePiece g9;
  ArrayList<GamePiece> r2;
  ArrayList<GamePiece> r3;
  ArrayList<GamePiece> r4;
  
  // Comparator Class
  EdgeSorting edgeSort = new EdgeSorting();
  
  // data for tests
  void init() {
  
    l1 = new LightEmAll(2, 2, new Random(5));
    g0 = l1.board.get(0).get(0);
    g1 = l1.board.get(0).get(1);
    g2 = l1.board.get(1).get(0);
    g3 = l1.board.get(1).get(1);
    r0 = new ArrayList<GamePiece>(Arrays.asList(g0, g1));
    r1 = new ArrayList<GamePiece>(Arrays.asList(g2, g3));
    
    l2 = new LightEmAll(2, 3, new Random(6));
    g4 = l2.board.get(0).get(0);
    g5 = l2.board.get(0).get(1);
    g6 = l2.board.get(1).get(0);
    g7 = l2.board.get(1).get(1);
    g8 = l2.board.get(2).get(0);
    g9 = l2.board.get(2).get(1);
    r2 = new ArrayList<GamePiece>(Arrays.asList(g4, g5));
    r3 = new ArrayList<GamePiece>(Arrays.asList(g6, g7));
    r4 = new ArrayList<GamePiece>(Arrays.asList(g8, g9));
    
    e0 = l1.allEdges.get(0);
    e1 = l1.allEdges.get(1);
    e2 = l1.allEdges.get(1);
    e3 = l1.allEdges.get(3);
    
  }
  
  //tests for the method compare in EdgeSorting class
  boolean testEdgeComparator(Tester t) {
    init();
    
    return t.checkExpect(edgeSort.compare(e1, e3), -1)
        && t.checkExpect(edgeSort.compare(e3, e3), 0);

  }
 
  /*      _____  
        .'     `.
       /         \
      |           | 
      '.  +^^^+  .'
        `. \./ .'
          |_|_|  
          (___)    
          (___)
          `---'
   *  LightEmAll Tests:
   */
  
  // tests for the method randomStart
  void testRandomStart(Tester t) {
    init();
    
    // tiles initially
    t.checkExpect(g0.top, false);
    t.checkExpect(g0.bottom, true);
    t.checkExpect(g0.left, true);
    t.checkExpect(g0.right, false);
    
    t.checkExpect(g1.top, true);
    t.checkExpect(g1.bottom, false);
    t.checkExpect(g1.left, false);
    t.checkExpect(g1.right, true);
  
    // randomizing
    l1.randomStart();
    
    // tiles change
    t.checkExpect(g0.top, true);
    t.checkExpect(g0.bottom, false);
    t.checkExpect(g0.left, true);
    t.checkExpect(g0.right, false);
    
    t.checkExpect(g1.top, true);
    t.checkExpect(g1.bottom, false);
    t.checkExpect(g1.left, true);
    t.checkExpect(g1.right, false);
    
  }
  
  // tests for the method kruskal
  void testKruskal(Tester t) {
    init();
    // mst is initialized with kruskal
    // testing to see if mst size is n - 1
    // and less than the original allEdges size
    Edge e0 = new Edge(g0, g1, 0);
    Edge e1 = new Edge(g1, g3, 0);
    Edge e2 = new Edge(g2, g0, 1);
    t.checkExpect(l1.allEdges.size(), 8);
    t.checkExpect(l1.mst.size(), 3);
    
    t.checkExpect(l1.mst, new ArrayList<Edge>(Arrays.asList(e0, e1, e2)));
    
    Edge e3 = new Edge(g4, g5, 0);
    Edge e4 = new Edge(g9, g7, 0);
    Edge e5 = new Edge(g4, g6, 1);
    Edge e6 = new Edge(g5, g7, 2);
    Edge e7 = new Edge(g8, g9, 2);
    t.checkExpect(l2.allEdges.size(), 14);
    t.checkExpect(l2.mst.size(), 5);
    
    t.checkExpect(l2.mst, new ArrayList<Edge>(Arrays.asList(e3, e4, e5, e6, e7)));
    
  }
  
  // tests for the method setReps
  void testSetReps(Tester t) {
    init();
    // manually setting representatives
    HashMap<GamePiece, GamePiece> l1Rep = new HashMap<GamePiece, GamePiece>();
    l1Rep.put(g0, g1);
    l1Rep.put(g1, g3);
    l1Rep.put(g2, g3);
    l1Rep.put(g3, g3);
    
    HashMap<GamePiece, GamePiece> l2Rep = new HashMap<GamePiece, GamePiece>();
    l2Rep.put(g4, g5);
    l2Rep.put(g5, g6);
    l2Rep.put(g6, g7);
    l2Rep.put(g8, g7);
    l2Rep.put(g9, g7);
    l2Rep.put(g7, g7);
    
    // representatives are initialized with setReps
    t.checkExpect(l1.representatives, l1Rep);
    t.checkExpect(l2.representatives, l2Rep);
  }
  
  // tests for the methods find and union
  void testUnionFind(Tester t) {
    init();
    // representatives are intializes with the same parents
    t.checkExpect(l2.find(g5), g7);
    t.checkExpect(l2.find(g8), g7);
    
    l2.union(g5, g8);
    
    t.checkExpect(l2.find(g5), g7);
    t.checkExpect(l2.find(g8), g7);
  }
  
  // tests for the method onTick
  void testOnTick(Tester t) {
    init();
    
    l1.seconds = 57;
    t.checkExpect(l1.seconds, 57);
    l1.onTick();
    t.checkExpect(l1.seconds, 58);
    l1.onTick();
    t.checkExpect(l1.seconds, 59);
    l1.onTick();
    t.checkExpect(l1.seconds, 0);
    t.checkExpect(l1.minutes, 1);
    
  }
  
  // tests for the method assignEdges
  void testAssignEdges(Tester t) {
    init();
    
    // manually creating edges that should be in list
    Edge e0 = new Edge(g0, g1, 0);
    Edge e1 = new Edge(g1, g3, 0);
    Edge e2 = new Edge(g2, g0, 1);
    Edge e3 = new Edge(g3, g1, 1);
    Edge e4 = new Edge(g0, g2, 2);
    Edge e5 = new Edge(g1, g0, 2);
    Edge e6 = new Edge(g2, g3, 3);
    Edge e7 = new Edge(g3, g2, 3);
    
    // allEdges list is initialized with assignEdges
    // testing to see if edges are assigned
    t.checkExpect(l1.allEdges, new ArrayList<Edge>(Arrays.asList(e0, e1, e2, e3, e4, e5, e6, e7)));
  }
  
  // test for the method secondsAsString and minutesAsString
  void testAsString(Tester t) {
    init();
    // 1 digit seconds 
    l1.seconds = 3;
    t.checkExpect(l1.secondsAsString(), "03");
    
    // 2 digit seconds
    l2.seconds = 13;
    t.checkExpect(l2.secondsAsString(), "13");
    
    // 1 digit minutes
    l1.minutes = 1;
    t.checkExpect(l1.minutesAsString(), "01");
    
    // 2 digit minutes
    l2.minutes = 13;
    t.checkExpect(l2.minutesAsString(), "13");
  }
  
  // tests for the method makeScene
  void testMakeScene(Tester t) {
    init();
    WorldImage clock = new TextImage("【00 : 00】", 30, FontStyle.BOLD, Color.DARK_GRAY);
    WorldImage clicks =  new TextImage("Rotations: 0", 25, FontStyle.BOLD, Color.DARK_GRAY);
    
    // drawing images using tileImage (provided to us)
    WorldImage g0Image = g0.tileImage(70, 5, Color.red, true);
    WorldImage g1Image = g1.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g2Image = g2.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g3Image = g3.tileImage(70, 5, Color.LIGHT_GRAY, false);
    
    // a regular scene with some powered tiles, 
    // regular tiles, and a power station
    WorldScene w1 = new WorldScene(2, 2);
    w1.placeImageXY(g0Image, 35, 35);
    w1.placeImageXY(g1Image, 35, 105);
    w1.placeImageXY(g2Image, 105, 35);
    w1.placeImageXY(g3Image, 105, 105);
    w1.placeImageXY(clock, 120, 175);
    w1.placeImageXY(clicks, 20, 175);
    t.checkExpect(l1.makeScene(), w1);
    
    // a winning scene
    init();
    // drawing images using tileImage
    WorldImage g4Image = g4.tileImage(70, 5, Color.red, true);
    WorldImage g5Image = g5.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g6Image = g6.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g7Image = g7.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g8Image = g8.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage g9Image = g9.tileImage(70, 5, Color.LIGHT_GRAY, false);
    WorldImage box = new RectangleImage(70, 60, OutlineMode.SOLID, Color.LIGHT_GRAY);
    WorldImage winText =  new AboveImage(new TextImage("YOU WIN!", 
        14, FontStyle.BOLD, Color.black), new AboveImage(new TextImage("Clicks: 0", 
            6, FontStyle.BOLD, Color.black), new TextImage("Press r to restart", 6, 
                FontStyle.BOLD, Color.black)));
    WorldImage winBox = new OverlayImage(winText, box);
    
    WorldScene w2 = new WorldScene(2, 3);
    l2.win = true;
    w2.placeImageXY(g4Image, 35, 35);
    w2.placeImageXY(g5Image, 35, 105);
    w2.placeImageXY(g6Image, 35, 175);
    w2.placeImageXY(g7Image, 105, 35);
    w2.placeImageXY(g8Image, 105, 105);
    w2.placeImageXY(g9Image, 105, 175);
    // win text should show up
    w2.placeImageXY(winBox, 70, 105);
    w2.placeImageXY(clock, 120, 245);
    w2.placeImageXY(clicks, 20, 245);
    t.checkExpect(l2.makeScene(), w2);
  }
  
  // tests for the method makeBoard and connectPieces
  boolean testMakeBoardConnectPieces(Tester t) {
    init();
    // board is initialized with makeBoard
    // testing to see if board has rows in list
    // also testing to see if list contains the GamePieces that have boolean
    // values assigned to them (connectPieces)
    return t.checkExpect(l1.board, new ArrayList<ArrayList<GamePiece>>(Arrays.asList(r0, r1)))
        && t.checkExpect(l2.board, new ArrayList<ArrayList<GamePiece>>(Arrays.asList(r2, r3, r4)));

  }
  
  // tests for the method equals (Override)
  void testNewEquals(Tester t) {
    init();
    // checking to see if inputting another Object automatically gives false
    t.checkExpect(g0.equals("hey"), false);
    // equal
    t.checkExpect(g1.equals(g1), true);
    // not equal
    t.checkExpect(g2.equals(g3), false);
  }
  
  // tests for the method hashCode (Override)
  void testNewHashCode(Tester t) {
    init();
    t.checkExpect(g0.hashCode(), 0);
    t.checkExpect(g1.hashCode(), 5000);
  }
 
  // tests for the method onMouseClicked
  void testOnMouseClicked(Tester t) {
    init();
    // right angle wire
    // wires prior
    t.checkExpect(g9.left, true);
    t.checkExpect(g9.bottom, true);
    t.checkExpect(g9.right, false);
    t.checkExpect(g9.top, false);
    
    l2.onMouseClicked(new Posn(70, 140));
    
    // wires after (top becomes original left and right left becomes bottom)
    t.checkExpect(g9.left, true);
    t.checkExpect(g9.bottom, false);
    t.checkExpect(g9.right, false);
    t.checkExpect(g9.top, true);
    
    // half top wire
    // wires prior
    t.checkExpect(g6.left, false);
    t.checkExpect(g6.bottom, false);
    t.checkExpect(g6.right, false);
    t.checkExpect(g6.top, true);
    
    l2.onMouseClicked(new Posn(0, 70));
    
    // wires after
    // top becomes ORIGINAL left value
    t.checkExpect(g6.left, false);
    t.checkExpect(g6.bottom, false);
    t.checkExpect(g6.right, true);
    t.checkExpect(g6.top, false);
  }

  // tests for the method onKeyEvent
  void testOnKeyEvent(Tester t) {
    init();
    // *** moving the power station around 
    
    //making connection for ability to move down
    l1.onMouseClicked(new Posn(0, 70));
    l1.onMouseClicked(new Posn(0, 70));
    l1.onMouseClicked(new Posn(0, 70));
    
    // moving down
    t.checkExpect(l1.powerRow, 0);
    t.checkExpect(l1.powerCol, 0);
    l1.onKeyEvent("down");
    t.checkExpect(l1.powerCol, 0);
    t.checkExpect(l1.powerRow, 1);
    
    // no connection constraint
    // (should not move right)
    l1.onKeyEvent("right");
    // stays in the same spot
    t.checkExpect(l1.powerRow, 1);
    t.checkExpect(l1.powerCol, 0);
    
    // trying to move down again
    // out of bounds constraint
    // (should stay in the same spot)
    l1.onKeyEvent("down");
    t.checkExpect(l1.powerRow, 1);
    t.checkExpect(l1.powerCol, 0);
    
    // rotating and then moving down 
    t.checkExpect(l2.powerRow, 0);
    t.checkExpect(l2.powerCol, 0);
    l2.onMouseClicked(new Posn(0, 0));
    l2.onKeyEvent("down");
    t.checkExpect(l2.powerRow, 1);
    
    // win constraint
    // (shouldn't move after winning game)
    init();
    t.checkExpect(l2.powerRow, 0);
    t.checkExpect(l2.powerCol, 0);
    
    // setting win to true
    // and making a connection to move right
    l2.onMouseClicked(new Posn(70, 0));
    l2.win = true;
    
    // trying to move station right
    l2.onKeyEvent("right");
    
    // still in the same place
    t.checkExpect(l2.powerRow, 0);
    t.checkExpect(l2.powerCol, 0);
    
    // restarting after winning
    ArrayList<GamePiece> l2Row = l2.board.get(1);
    t.checkExpect(l2.board.get(1), l2Row);
    // calls m1.restartGame
    l2.onKeyEvent("r");
    // new row has been created, shows has restarted
    t.checkExpect((l2.board.get(1) == l2Row), false);
    
  }
 
  // tests for the method lightEmUp (BFS)
  void testLightEmUp(Tester t) {
    init();
    // making sure board starts with necessary wires powered
    t.checkExpect(g4.powered, true);
    
    
    // (noting disconnected tiles)
    t.checkExpect(g5.powered, false);
    t.checkExpect(g6.powered, false);
    t.checkExpect(g7.powered, false);
    t.checkExpect(g8.powered, false);
    t.checkExpect(g9.powered, false);
    
    // rotating disconnected tiles 
    // and making sure the BFS works
    // (aka lights them up)
    l2.onMouseClicked(new Posn(70, 0));
    l2.onMouseClicked(new Posn(70, 70));
    l2.onMouseClicked(new Posn(70, 140));
    
    // connection made
    // g5, 7, 8, 9 should be lit
    l2.lightEmUp();
    t.checkExpect(g5.powered, true);
    t.checkExpect(g7.powered, true);
    t.checkExpect(g8.powered, true);
    t.checkExpect(g9.powered, true);
    
    // checking if the BFS un-lights wires as well
    l2.onMouseClicked(new Posn(70, 70));
    
    // disconnected
    // g9 should be un-lit
    l2.lightEmUp();
    t.checkExpect(g7.powered, false);
    t.checkExpect(g8.powered, false);
    t.checkExpect(g9.powered, false);
    
    // connecting all 
    l2.onMouseClicked(new Posn(70, 70));
    l2.onMouseClicked(new Posn(0, 0));
    
    // everything should be powered
    l2.lightEmUp();
    t.checkExpect(g4.powered, true);
    t.checkExpect(g5.powered, true);
    t.checkExpect(g6.powered, true);
    t.checkExpect(g7.powered, true);
    t.checkExpect(g8.powered, true);
    t.checkExpect(g4.powered, true);
    t.checkExpect(g9.powered, true);
  }

  // tests for the method winCondition
  void testWinCondition(Tester t) {
    init();
    t.checkExpect(l2.win, false);
    
    // winning by making everything powered
    l2.onMouseClicked(new Posn(0, 0));
    l2.onMouseClicked(new Posn(70, 0));
    l2.onMouseClicked(new Posn(70, 70));
    l2.onMouseClicked(new Posn(70, 140));
    
    l2.winCondition();
    
    t.checkExpect(l2.win, true);
    
    // making sure game isn't won when it shouldn't be
    t.checkExpect(l1.win, false);
    
    // not winning 
    l1.onMouseClicked(new Posn(0, 0));
    l1.onMouseClicked(new Posn(70, 70));
    l1.onMouseClicked(new Posn(0, 70));
    
    l1.winCondition();
    
    t.checkExpect(l1.win, false);
    
  }
  
  /*  +------------+
   *  |     ||     |
   *  |     ||     |
   *  |============|
   *  |     ||     |
   *  |     ||     |
   *  +------------+
   *  GamePiece Tests:
   */
  
  // the method tileImage was provided from assignment!
  
  // tests for the method rotate
  void testRotate(Tester t) {
    init();
    // right angle wire
    // wires prior
    t.checkExpect(g9.left, true);
    t.checkExpect(g9.bottom, true);
    t.checkExpect(g9.right, false);
    t.checkExpect(g9.top, false);
    
    g9.rotate();
    
    // wires after (top becomes original left and right left becomes bottom)
    t.checkExpect(g9.left, true);
    t.checkExpect(g9.bottom, false);
    t.checkExpect(g9.right, false);
    t.checkExpect(g9.top, true);
    
    // half top wire
    // wires prior
    t.checkExpect(g6.left, false);
    t.checkExpect(g6.bottom, false);
    t.checkExpect(g6.right, false);
    t.checkExpect(g6.top, true);
    
    g6.rotate();
    
    // wires after
    // top becomes ORIGINAL left value
    t.checkExpect(g6.left, false);
    t.checkExpect(g6.bottom, false);
    t.checkExpect(g6.right, true);
    t.checkExpect(g6.top, false);
  }
  
  // tests for the method hasX
  boolean testHasX(Tester t) {
    init();
    return t.checkExpect(g0.hasBottom(), true)
        && t.checkExpect(g8.hasBottom(), false)
        
        && t.checkExpect(g1.hasTop(), true)
        && t.checkExpect(g9.hasTop(), false)
        
        && t.checkExpect(g7.hasLeft(), true)
        && t.checkExpect(g5.hasLeft(), false)
        
        && t.checkExpect(g5.hasRight(), true)
        && t.checkExpect(g6.hasRight(), false);
  }
  
  // tests for the method xStation
  void testxStation(Tester t) {
    init();
    // place
    t.checkExpect(g1.powerStation, false);
    g1.placeStation();
    t.checkExpect(g1.powerStation, true);
    
    // remove
    t.checkExpect(g4.powerStation, true);
    g4.removeStation();
    t.checkExpect(g4.powerStation, false);
  }
  
  // tests for the method powerX
  void testPowerX(Tester t) {
    init();
    // on
    t.checkExpect(g8.powered, false);
    g8.powerOn();
    t.checkExpect(g8.powered, true);
    
    // off
    t.checkExpect(g0.powered, true);
    g0.powerOff();
    t.checkExpect(g0.powered, false);
  } 
}
