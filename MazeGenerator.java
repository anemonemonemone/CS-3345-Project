// CS 3345, Wei Yuan Liew, wxl220016
// This program generates a maze, and at the press of a button, gives out the solution
import java.util.*;

// Disjoint set implementation
class DisjointSet{
    private int[] parent;
    private int numSets;

    // initialization
    public DisjointSet(int n){
        parent = new int[n];
        numSets = n;

        for(int i = 0; i < n; i++){
            parent[i] = -1;
        }
    }

    // recursive find operation (lacks path compression)
    public int find(int x){
        if(parent[x] < 0){
            return x;
        }
        else{
            return find(parent[x]);
        }
    }

    // union operation
    public void union(int x, int y){
        int rootX = find(x);
        int rootY = find(y);

        // both elements share the same parent
        if (rootX == rootY){
            return;
        }
        // elements do not share the same parent
        else{
            if(parent[rootX] <= parent[rootY]){
                parent[rootX] += parent[rootY];
                parent[rootY] = rootX;
            }
            else{
                parent[rootY] += parent[rootX];
                parent[rootX] = rootY;
            }
            numSets--;
        }
    }

    // return number of disjoint sets left
    public int getNumSets(){
        return numSets;
    }
}

public class MazeGenerator {
    private int row;
    private int col;
    private int[][] maze;
    private boolean[][] visited;
    private DisjointSet disjointSet;
    private List<Integer> path;
    private static final int WALL_NORTH = 0b1000;
    private static final int WALL_WEST = 0b0100;
    private static final int WALL_SOUTH = 0b0010;
    private static final int WALL_EAST = 0b0001;

    // initialization
    public MazeGenerator(int row, int col){
        this.row = row;
        this.col = col;
        maze = new int[row][col];
        visited = new boolean[row][col];
        disjointSet = new DisjointSet(row*col);
        path = new ArrayList<>();

        for(int i = 0; i < row; i++){
            for(int j = 0; j< col; j++){
                maze[i][j] = 0b1111;
            }
        }

        // marks that the first and last cell is start and end
        maze[0][0] = 0b0000;
        maze[row-1][col-1] = 0b0000;
    }

    // helper functions
    // determine which element in disjoint set to access
    private int getIndex(int row, int col){
        return row * this.col + col;
    }
    // randomization of choosing which disjoint set to access
    private int getRandomIndex(){
        Random random = new Random();
        return random.nextInt(row*col-1)+1;
    }
    // converts a given index in disjoint set into corresponding row & col value in matrix
    private int getRow(int index){
        return index / col;
    }
    private int getCol(int index){
        return index % col;
    }

    public void generateMaze(){
        // set first & last cell to be start and end
        int[] walls = {WALL_EAST, WALL_SOUTH, WALL_WEST, WALL_NORTH}; // representing east, south, west, and north walls

        // while not all the cells in maze is connected,
        while(disjointSet.getNumSets() > 1){
            // randomly choose a cell to break down walls
            int randomCell = getRandomIndex();
            int randomRow = getRow(randomCell);
            int randomCol = getCol(randomCell);

            // shuffle to see which walls to take down
            Collections.shuffle(Arrays.asList(walls));
            for(int wall : walls){
                int nextRow = randomRow;
                int nextCol = randomCol;
                int adjWall = 0;

                // check for which wall to take down and increment row or column correspondingly
                if (wall == WALL_EAST){
                    nextCol++;
                    adjWall = WALL_WEST;    // to make sure adjacent cell's wall are taken down too
                } else if (wall == WALL_SOUTH) {
                    nextRow++;
                    adjWall = WALL_NORTH;
                } else if (wall == WALL_WEST) {
                    nextCol--;
                    adjWall = WALL_EAST;
                } else if (wall == WALL_NORTH) {
                    nextRow--;
                    adjWall = WALL_SOUTH;
                }

                // check if adjacent cell of random cell is within the matrix
                if (nextRow >= 0 && nextRow < row && nextCol >= 0 && nextCol < col) {
                    // get the adjacent cell's row and col
                    int nextCell = getIndex(nextRow, nextCol);

                    // if the given cells are disjoint, union them together
                    if (disjointSet.find(randomCell) != disjointSet.find(nextCell)) {
                        maze[randomRow][randomCol] &= ~wall; // remove wall in current cell
                        maze[nextRow][nextCol] &= ~adjWall; // remove wall in next cell

                        disjointSet.union(randomCell, nextCell);
                    }
                }
            }
        }
    }

    // solves the maze
    void solveMaze(){
        dfs(0,0);
    }

    // runs a dfs of the cell to look for the solution
    private void dfs(int row, int col) {
        // if given row or col is out of bounds, exit recursion as it has reached a dead end
        if (row < 0 || row >= this.row || col < 0 || col >= this.col) {
            return;
        }
        // to prevent infinite loop inside the cell and look for unvisited cell for solution
        if (visited[row][col]){
            return;
        }
        // marks a cell visited
        visited[row][col] = true;
        // adds the cell into the path list
        path.add(getIndex(row,col));
        // if end of matrix is reached, print solution and exit recursion
        if (row == this.row - 1 && col == this.col - 1) {
            printSolution();
            return;
        }
        // checks for the walls of a given cell
        int walls = maze[row][col];
        // if a wall of given direction is missing, recursively call dfs with appropriate row and col
        if ((walls & WALL_NORTH) == 0) {
            dfs(row - 1, col); // go up
        }
        if ((walls & WALL_EAST) == 0) {
            dfs(row, col + 1); // go right
        }
        if ((walls & WALL_SOUTH) == 0) {
            dfs(row + 1, col); // go down
        }
        if ((walls & WALL_WEST) == 0) {
            dfs(row, col - 1); // go left
        }
        visited[row][col] = false; // marks the visited cell false when it exits from dead end
        path.remove(path.size()-1); // remove given cell from the list
    }

    // prints out the generated maze
    public void printMaze() {
        System.out.print("    ");
        for(int i = 1; i < col; i++){
            System.out.print("+---");
        }
        System.out.println("+");
        for(int i = 0; i < row; i++){
            System.out.print("|");

            for(int j = 0; j < col; j++){
                if((maze[i][j] & WALL_EAST) == WALL_EAST){
                    System.out.print("   |");
                }
                else{
                    System.out.print("    ");
                }
            }
            System.out.print("\n");
            for(int j = 0; j < col; j++){
               if((maze[i][j] & WALL_SOUTH) == WALL_SOUTH){
                   System.out.print("+---");
               }
               else{
                   System.out.print("    ");
               }
            }
            System.out.print("+\n");
        }
    }

    // prints out the solution
    public void printSolution(){
        System.out.println("The path you should take in direction looks like: ");
        // calculates the direction of next cell needed to be visited based off current cell and print direction
        for(int i = 0; i < path.size()-1;i++){
            int row = getRow(path.get(i));
            int col = getCol(path.get(i));

            int nextRow = getRow(path.get(i+1));
            int nextCol = getCol(path.get(i+1));

            if(nextRow-row == 1){
                System.out.print("S");
            }
            if(nextRow-row == -1){
                System.out.print("N");
            }
            if(nextCol-col == 1){
                System.out.print("E");
            }
            if(nextCol-col == -1){
                System.out.print("W");
            }
        }
        System.out.println();

        // prints out the pathing to solve the maze
        System.out.println("Visualized: ");
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(visited[i][j]){
                    System.out.print("1");
                }
                else{
                    System.out.print("0");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        System.out.print("Please enter n value: ");
        int n = input.nextInt();
        System.out.print("Please enter m value: ");
        int m = input.nextInt();

        MazeGenerator maze = new MazeGenerator(n,m);
        maze.generateMaze();
        System.out.println("Generated maze looks like: ");
        maze.printMaze();
        System.out.print("Enter 0 to generate the solution: ");
        input.nextInt();
        maze.solveMaze();
    }
}
