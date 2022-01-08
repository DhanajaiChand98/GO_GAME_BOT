import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

class alpha_beta_player{

    static final int DEPTH_LIMIT = 4;
    static final String MOVES_FILE = "moves.txt";
    static final String INPUT_FILE = "input.txt";
    static final String OUTPUT_FILE = "output.txt";
    static final int PASS = 0;
    static final int ACTION = 1;

    static int size, moveLimit, komi, depth_limit, player_piece_type;
    static int[][] global_previous_board, options, global_board;
    
    public static void readInput(String filePath){

        // pass the path to the file as a parameter
        File file;
        Scanner sc;
        global_board = new int[size][size];
        global_previous_board = new int[size][size];
        
        try {
            file = new File(filePath);
            sc = new Scanner(file);
            player_piece_type = sc.nextInt();
            sc.nextLine();

            for(int i=0; i<size; i++){
                String temp = sc.nextLine();
                int j=0;
                for(char c : temp.toCharArray()){
                    global_previous_board[i][j++] = c - '0';
                }

            }

            for(int i=0; i<size; i++){
                String temp = sc.nextLine();
                int j=0;
                for(char c : temp.toCharArray()){
                    global_board[i][j++] = c - '0';
                }
            }

            sc.close();

        } 
        catch (FileNotFoundException e) {
            System.out.println("Cannot read file: " + filePath);
            e.printStackTrace();
        }
    }

    public static void writeOutput(String filePath, boolean isPass, int x, int y){

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            if(isPass){
                fileWriter.write("PASS");
            }
            else{
                fileWriter.write(String.valueOf(x) + "," + String.valueOf(y));
            }
            fileWriter.close();
        } 
        catch (Exception e) {
            System.out.println("Cannot write file : " + filePath);
        }

    }

    public static String encodedState(int[][] board){
        
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<board.length; i++){
            for(int j=0; j<board[0].length-1; i++){
                sb.append(String.valueOf(board[i][j]));
            } 
        }
        return sb.toString();

    }

    public static boolean isEmpty(int[][] board){

        for(int i=0;i<board.length;i++){
            for(int j=0;j<board.length;j++){
                if(board[i][j] != 0){
                    return false;
                }
            }
        }
        return true;
    }

    public static void writeMovesToFile(int moves){
        try {
            FileWriter fileWriter = new FileWriter(MOVES_FILE);
            fileWriter.write(String.valueOf(moves));
            fileWriter.close();
        } 
        catch (Exception e) {
            System.out.println("Cannot write file moves.txt");
        }
    }

    public static int readMovesFromFile(){
        
        File file;
        Scanner sc;
        try {
            file = new File(MOVES_FILE);
            sc = new Scanner(file);
            int returnVal = sc.nextInt();
            sc.close();
            return returnVal;
        } 
        catch (FileNotFoundException e) {
            System.out.println("Cannot read file moves.txt");
        }

        return 0;
    }

    public static ArrayList<Integer> move(int prev_board[][], int board[][], int player_piece_type){

        ArrayList<Integer> result = null;
        
        if(isEmpty(board)){
            writeMovesToFile(1);
            result = new ArrayList<>();
            result.add(2);
            result.add(2);
            return result;
        }
        else if(isEmpty(prev_board)){
            for(int i=0;i<options.length; i++){
                int x = options[i][0];
                int y = options[i][1];
                if(board[x][y] == 0){
                    writeMovesToFile(2);
                    result = new ArrayList<>();
                    result.add(x);
                    result.add(y);
                    return result;
                }
            }     
        }

        int moves = readMovesFromFile();
        moves++;

        ArrayList<Integer> nextMove = onTheGoAlphaBeta(prev_board, board, player_piece_type, moves);

        writeMovesToFile(moves + 1);
        return nextMove;

    }

    public static int[][] clone2DArray(int[][] arr){

        int [][] arrClone = new int[arr.length][];
        for(int i = 0; i < arr.length; i++)
        {
            int[] temp = arr[i];
            int temp_length = temp.length;
            arrClone[i] = new int[temp_length];
            System.arraycopy(temp, 0, arrClone[i], 0, temp_length);
        }
        return arrClone;

    }

    public static ArrayList<Integer> onTheGoAlphaBeta(int[][] prev_state, int[][] state, int piece_type, int numMoves){

        int[][] state_copy = clone2DArray(state);
        int[][] prev_state_copy = clone2DArray(prev_state);
        double alpha = Integer.MIN_VALUE;
        double beta = Integer.MAX_VALUE;

        ArrayList<Integer> nextMove = new ArrayList<>();

        ReturnValueEncapsulator rv = alphaBeta(true, prev_state_copy, state_copy, 0, ACTION, numMoves, piece_type, alpha, beta);

        if(rv.moveType == PASS){
            return null;
        }
        else{
            nextMove.add(rv.move[0]);
            nextMove.add(rv.move[1]);
            return nextMove;
        }
    
    } 

    private static boolean isNeighbourEmpty(int i, int j, int[][] state){

        if ((i - 1 >= 0 && state[i - 1][j] == 0) || (i + 1 < size && state[i + 1][j] == 0)){
            return true;
        }
        if ((j - 1 >= 0 && state[i][j - 1] == 0) || (j + 1 < size && state[i][j + 1] == 0)){
            return true;
        }

        return false;

    }

    private static boolean isConnected(int i, int j, int state[][], int piece_type){
        if ((i - 1 >= 0 && state[i - 1][j] == piece_type) || (i + 1 < size && state[i + 1][j] == piece_type)){
            return true;
        }
        if ((j - 1 >= 0 && state[i][j - 1] == piece_type) || (j + 1 < size && state[i][j + 1] == piece_type)){
            return true;
        }
        if(i - 1 >= 0 && j + 1 < size && state[i-1][j] == piece_type){
            return true;
        }
        if(i - 1 >= 0 && j - 1 >= 0 && state[i-1][j] == piece_type){
            return true;
        }
        if(i + 1 < size && j + 1 < size && state[i+1][j] == piece_type){
            return true;
        }
        if(i + 1 < size && j - 1 >= 0 && state[i+1][j] == piece_type){
            return true;
        }    
        return false;

    }

    public static Double minMaxEvaluationHeuristic(int[][] state, int depth, int numMoves){

        int playerType = 1;
        int opponentType = 2;

        if (player_piece_type == 2){
            playerType = 2;
            opponentType = 1;
        }

        int playerCount = 0, opponentCount = 0;
        int playerNeighbourEmpty = 0, opponentNeighbourEmpty = 0;
        int playerEdgePlaces = 0, opponentEdgePlaces = 0;
        int playerCornerPlaces = 0,  opponentCornerPlaces = 0;
        int playerDeaths = 0, opponentDeaths = 0;
        int playerEyes = 0, opponentEyes = 0;
        int playerConnections = 0, opponentConnections = 0;
        int playerInEyes = 0;
        int playerLiberty = 0, opponentLiberty = 0;
        int playerKiteCount = 0, opponentKiteCount = 0;
        int playerFullRhombus = 0, opponentFullRhombus = 0;
        int Q1 = 0, Q3 = 0, Qd = 0;

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){

                if(state[i][j] == playerType){
                    playerCount++;

                    boolean isPlayerConnected = false;
                    // player in eyes
                    ArrayList<ArrayList<Integer>> neigbours_8 = detect_8neighbors(state, i, j);
                    boolean isEye = true;
                    for(ArrayList<Integer> n : neigbours_8){

                        if(state[n.get(0)][n.get(1)] != playerType){
                            isEye = false;
                            break;
                        }
                        else{
                            isPlayerConnected = true;
                        }

                    }

                    ArrayList<ArrayList<Integer>> neigbours_4 = detect_8neighbors(state, i, j);
                    for(ArrayList<Integer> n : neigbours_4){
                        playerConnections++;

                    }

                    if(isEye){
                        playerInEyes++;
                    }
                    
                    if(isPlayerConnected){
                        playerConnections+=0;
                    }
                    
                    if ((i==0 && j==0) || (i==size-1 && j==0) || (i==0 && j==size-1) || (i==size-1 && j==size-1)){
                        playerCornerPlaces++;
                    }     
                    else if(i == 0 || j == 0 || i == size - 1 || j == size - 1){
                        playerEdgePlaces++;
                    }
                    
                    if(find_liberty(state, i, j)){
                        playerLiberty++;
                    }

                }
                else if (state[i][j] == opponentType){
                    opponentCount++;
                    if(isConnected(i, j, state, opponentType)){
                        opponentConnections++;
                    }
                    if(find_liberty(state, i, j)){
                        opponentLiberty++;
                    }
                }

                if(global_board[i][j] == playerType && state[i][j] != playerType){
                    
                    playerDeaths++;
                }
                else if(global_board[i][j] == opponentType && state[i][j] != opponentType){
                    opponentDeaths++;
                }
                
                if(i-1 >= 0 && j-1 >=0 && state[i-1][j-1] == state[i][j]){
                    playerEyes += state[i][j] == playerType ? 1 : 0;
                    opponentEyes += state[i][j] == opponentType ? 1 : 0;
                }
                if(i-1 >= 0 && j+1 < size && state[i-1][j+1] == state[i][j]){
                    playerEyes += state[i][j] == playerType ? 1 : 0;
                    opponentEyes += state[i][j] == opponentType ? 1 : 0;
                }
                if(i+1 < size && j-1 >=0 && state[i+1][j-1] == state[i][j]){
                    playerEyes += state[i][j] == playerType ? 1 : 0;
                    opponentEyes += state[i][j] == opponentType ? 1 : 0;
                }
                if(i+1 < size && j+1 < size && state[i+1][j+1] == state[i][j]){
                    playerEyes += state[i][j] == playerType ? 1 : 0;
                    opponentEyes += state[i][j] == opponentType ? 1 : 0;
                }

                if (i - 1 >= 0 && j - 1 >= 0 && i + 1 < size  && j + 1 < size && state[i-1][j] == state[i+1][j] 
                    && state[i][j-1] == state[i][j+1] && state[i-1][j] == state[i][j-1]){
                    
                    if(state[i][j] == 0){
                        if(state[i-1][j] == playerType){
                            playerFullRhombus++;
                        }
                        else if(state[i-1][j] == opponentType){
                            opponentFullRhombus++;
                        }
                    }
                    else{
                        if(state[i-1][j] == playerType){
                            playerKiteCount++;
                        }
                        else if(state[i-1][j] == opponentType){
                            opponentKiteCount++;
                        }
                    }
                   
                }

                if(state[i][j] != playerType){

                    int count = 1;
                    if(i -1 >= 0 && j-1 >=0 && state[i-1][j-1] == state[i][j]){
                        count++;
                    }
                    if(i -1 >= 0 && state[i-1][j] == state[i][j]){
                        count++;
                    }
                    if(j - 1 >= 0 && state[i][j-1] == state[i][j]){
                        count++;
                    }
                    if(count == 1){
                        Q1++;
                    }
                    else if(count == 3){
                        Q3++;
                    }
                    else if(count == 2 && i - 1 >= 0 && j - 1 >= 0 && ((state[i-1][j-1] == state[i][j]) || (state[i-1][j] == state[i][j-1]))){
                        Qd++;
                    }
                     
                }
            }
        }

        // komi
        if(playerType == 1){
            opponentCount += 2.5;
        }
        else{
            playerCount += 0;
        }

        double pieceCountHeuristic = 2 * (playerCount - opponentCount);
        double neighbourEmptyHeuristic = 0 * (playerNeighbourEmpty - opponentNeighbourEmpty);
        double libertyHeuristic = 2 * (playerLiberty - (opponentLiberty));
        double numEdgePlacesHeuristic = (-1 * (double)playerEdgePlaces)  + (-4 * (double)playerCornerPlaces); //(-0.2 * (double)playerEdgePlaces)  + (-0.7 * (double)playerCornerPlaces);
        double killsHeuristics = 40 * (opponentDeaths) - 40 * (playerDeaths);
        double makeEyesHeuristics = 4.5 * (playerEyes - opponentEyes);
        double inEyeHeuristic = -15 * (playerInEyes);
        double connectionHeuristic = 2 *  (playerConnections);
        double kiteHeuristic = -15 * (playerKiteCount - opponentKiteCount);
        double rhombusHeuristic = 5.5 * (playerFullRhombus - opponentFullRhombus);
        double euler = 0;

        if(numMoves <= 9){
            numEdgePlacesHeuristic *= 200;
        }
        
        if(numMoves >= 20){
            pieceCountHeuristic *= 4;
        }
        
        if(playerType == 1){
            killsHeuristics *= 4;
        }

        return pieceCountHeuristic + libertyHeuristic + numEdgePlacesHeuristic + makeEyesHeuristics + 
        killsHeuristics + inEyeHeuristic + neighbourEmptyHeuristic + connectionHeuristic + kiteHeuristic + rhombusHeuristic + euler;

    }

    public static ArrayList<ArrayList<Integer>> detect_8neighbors(int[][] board, int i, int j){
        
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>();

        if (i > 0) {
            temp = new ArrayList<>();
            temp.add(i-1);
            temp.add(j);
            neighbours.add(temp);
        }
        if (i < size - 1){
            temp = new ArrayList<>();
            temp.add(i+1);
            temp.add(j);
            neighbours.add(temp);
        }
        if (j > 0){
            temp = new ArrayList<>();
            temp.add(i);
            temp.add(j-1);
            neighbours.add(temp);
        }
        if (j < size - 1){
            temp = new ArrayList<>();
            temp.add(i);
            temp.add(j+1);
            neighbours.add(temp);
        }
        if (i-1 >= 0 && j-1 >= 0){
            temp = new ArrayList<>();
            temp.add(i-1);
            temp.add(j-1);
            neighbours.add(temp);
        }
        if(i-1 >= 0 && j+1 < size){
            temp = new ArrayList<>();
            temp.add(i-1);
            temp.add(j+1);
            neighbours.add(temp);
        }
        if(i+1 < size && j-1 >= 0){
            temp = new ArrayList<>();
            temp.add(i+1);
            temp.add(j-1);
            neighbours.add(temp);
        }
        if(i+1 < size && j+1 < size){
            temp = new ArrayList<>();
            temp.add(i+1);
            temp.add(j+1);
            neighbours.add(temp);
        }

        return neighbours;
    }

    public static Double checkEnd(int[][] prevState, int[][] state, int moveType, int numMoves, int depth){

        if((isBaordEqual(prevState, state) && moveType == PASS) || numMoves > moveLimit ){
            if(player_piece_type == 2){
                return minMaxEvaluationHeuristic(state, depth, numMoves);
            }
            else{
                return minMaxEvaluationHeuristic(state, depth, numMoves);
            }
        }
        else{
            return null;
        }

    }

    static class ReturnValueEncapsulator{
        double val;
        int move[];
        int moveType;
        ReturnValueEncapsulator(double val, int move[], int moveType){
            this.val = val;
            this.move = move;
            this.moveType = moveType;
        }
    }

    public static ReturnValueEncapsulator alphaBeta(boolean isMax, int[][] prevState, int[][] state, int depth, int moveType, int numMoves, 
                                        int piece_type, double alpha, double beta){
        
        Double endReward = checkEnd(prevState, state, moveType, numMoves, depth);

        if(endReward != null){
            return new ReturnValueEncapsulator(endReward, null, PASS);
        }
        else if(depth >= depth_limit){
            // depth limit is reached
            return new ReturnValueEncapsulator(minMaxEvaluationHeuristic(state, depth, numMoves), null, PASS);
        }
        if(isMax){
            int[] maxMove = null;
            int maxMoveType = PASS;

            double v = Integer.MIN_VALUE;

            ReturnValueEncapsulator rv;

            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){

                    if(state[i][j] != 0){
                        continue;
                    }

                    // checking validity
                    if (!valid_place_check(i, j, piece_type, state, prevState)){
                        state[i][j] = 0;
                        continue;
                    }
                    
                    int[][] newPrevious = clone2DArray(state);
                    state[i][j] = piece_type;

                    int[][] clonedArray = clone2DArray(state);
                    remove_died_pieces(clonedArray, piece_type == 1 ? 2 : 1);

                    rv = alphaBeta(!isMax, newPrevious, clonedArray, depth + 1, ACTION, numMoves + 1, 
                                piece_type == 1 ? 2 : 1, alpha, beta);
                    
                    state[i][j] = 0;

                    if(rv.val >= v){
                        v = rv.val;
                        maxMove = new int[]{i, j};
                        maxMoveType = ACTION;
                    }
                        
                    alpha = Math.max(alpha, v);
                }

            }

            if(maxMove == null){
                // passing
                rv = alphaBeta(!isMax, clone2DArray(state), clone2DArray(state), depth + 1, ACTION, numMoves + 1, 
                                piece_type == 1 ? 2 : 1, alpha, beta);
                return new ReturnValueEncapsulator(rv.val, maxMove, PASS); 
            }

            return new ReturnValueEncapsulator(v, maxMove, maxMoveType);

        }
        else{

            int[] minMove = null;
            int minMoveType = PASS;

            double v = Integer.MAX_VALUE;
            ReturnValueEncapsulator rv;

            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){

                    if(state[i][j] != 0){
                        continue;
                    }

                    // checking validity
                    if (!valid_place_check(i, j, piece_type, state, prevState)){
                        state[i][j] = 0;
                        continue;
                    }

                    int[][] newPrevious = clone2DArray(state);
                    state[i][j] = piece_type;

                    int[][] clonedArray = clone2DArray(state);
                    remove_died_pieces(clonedArray, piece_type == 1 ? 2 : 1);
                    
                    rv = alphaBeta(!isMax, newPrevious, clonedArray, depth + 1, ACTION, numMoves + 1, 
                                piece_type == 1 ? 2 : 1, alpha, beta);
                    
                    state[i][j] = 0;

                    if(rv.val <= v){
                        v = rv.val;
                        minMove = new int[]{i, j};
                        minMoveType = ACTION;
                    }

                    // if (v <= alpha){
                    //     break;
                    // }

                    beta = Math.min(beta, v);
                }

            }

            if(minMove == null){

                rv = alphaBeta(!isMax, clone2DArray(state), clone2DArray(state), depth + 1, ACTION, numMoves + 1, 
                                piece_type == 1 ? 2 : 1, alpha, beta);
                return new ReturnValueEncapsulator(rv.val, minMove, PASS);
            
            }

            return new ReturnValueEncapsulator(v, minMove, minMoveType);
        }

    }

    public static boolean find_liberty(int[][] board, int i, int j){

        ArrayList<ArrayList<Integer>> members = ally_dfs(board, i, j);
        for (ArrayList<Integer> m : members){
            ArrayList<ArrayList<Integer>> neighbours = detect_neighbor(board, m.get(0), m.get(1));
            for (ArrayList<Integer> n : neighbours){
                if(board[n.get(0)][n.get(1)] == 0){
                    return true;
                }
            }
        }
        return false;

    }

    public static ArrayList<ArrayList<Integer>> detect_neighbor(int[][] board, int i, int j){
        
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>();

        if (i > 0) {
            temp = new ArrayList<>();
            temp.add(i-1);
            temp.add(j);
            neighbours.add(temp);
        }
        if (i < size - 1){
            temp = new ArrayList<>();
            temp.add(i+1);
            temp.add(j);
            neighbours.add(temp);
        }
        if (j > 0){
            temp = new ArrayList<>();
            temp.add(i);
            temp.add(j-1);
            neighbours.add(temp);
        }
        if (j < size - 1){
            temp = new ArrayList<>();
            temp.add(i);
            temp.add(j+1);
            neighbours.add(temp);
        }

        return neighbours;
    }

    public static ArrayList<ArrayList<Integer>> detect_neighbor_ally(int[][] board, int i, int j){

        ArrayList<ArrayList<Integer>> neighbours = detect_neighbor(board, i, j);
        ArrayList<ArrayList<Integer>> group_allies = new ArrayList<>();

        ArrayList<Integer> temp;

        for(ArrayList<Integer> n : neighbours){
            if(board[n.get(0)][n.get(1)] == board[i][j]){
                temp = new ArrayList<>();
                temp.add(n.get(0));
                temp.add(n.get(1));
                group_allies.add(temp);
            }
        }

        return group_allies;

    }

    public static ArrayList<ArrayList<Integer>> ally_dfs(int[][] board, int i, int j){

        Stack<ArrayList<Integer>> st = new Stack<>();
        
        ArrayList<Integer> startingIndexes = new ArrayList<>();
        startingIndexes.add(i);
        startingIndexes.add(j);
        st.push(startingIndexes);

        ArrayList<ArrayList<Integer>> ally_members = new ArrayList<>();

        while (!st.isEmpty()){

            ArrayList<Integer> p = st.pop();
            
            ally_members.add(p);
            
            ArrayList<ArrayList<Integer>> neighbour_allies = detect_neighbor_ally(board, p.get(0), p.get(1));
            
            for(ArrayList<Integer> ally : neighbour_allies){
                
                boolean inStackOrAllyMembers = false;
                for(ArrayList<Integer> temp : st){
                    if(temp.get(0) == ally.get(0) && temp.get(1) == ally.get(1)){
                        inStackOrAllyMembers = true;
                        break;
                    }
                }

                if(inStackOrAllyMembers){
                    continue;
                }

                for(ArrayList<Integer> temp : ally_members){
                    if(temp.get(0) == ally.get(0) && temp.get(1) == ally.get(1)){
                        inStackOrAllyMembers = true;
                        break;
                    }
                }

                if(inStackOrAllyMembers){
                    continue;
                }
                
                st.push(ally);
                
            }

        }

        return ally_members;

    }

    public static ArrayList<ArrayList<Integer>> find_died_pieces(int[][] board, int piece_type){
        
        ArrayList<ArrayList<Integer>> died_pieces = new ArrayList<>();
        ArrayList<Integer> temp;

        for (int i=0;i<size;i++){
            for (int j=0;j<size;j++){
                if(board[i][j] == piece_type){
                    if(!find_liberty(board, i, j)){
                        temp = new ArrayList<>();
                        temp.add(i);
                        temp.add(j);
                        died_pieces.add(temp);
                    }
                }
            }
        }

        return died_pieces;
    }


    public static ArrayList<ArrayList<Integer>> remove_died_pieces(int[][] board, int piece_type){

        ArrayList<ArrayList<Integer>> died_pieces = find_died_pieces(board, piece_type);
        if( died_pieces == null || died_pieces.size() == 0){
            return null;
        }

        remove_certain_pieces(board, died_pieces);
        return died_pieces;

    }

    public static void remove_certain_pieces(int[][] board, ArrayList<ArrayList<Integer>> positions){

        for(ArrayList<Integer> p : positions){
            board[p.get(0)][p.get(1)] = 0;
        }

    }

    public static boolean compare_board(int[][] board1, int[][] board2){
        
        for (int i=0;i<size;i++){
            for (int j=0;j<size;j++){
                if(board1[i][j] != board2[i][j]){
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBaordEqual(int[][] b1, int[][] b2){

        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                if(b1[i][j] != b2[i][j]){
                    return false;
                }
            }
        }
        return true;
    
    }

    public static boolean valid_place_check(int i, int j, int piece_type, int[][] state, int[][] prevState){

        if (!(i >= 0 && i < size) || !(j >= 0 && j < size)){
            return false;
        }

        if(prevState[i][j] != 0){
            return false;
        }

        int[][] test_state = clone2DArray(state);

        test_state[i][j] = piece_type;

        if(find_liberty(test_state, i, j)){
            return true;
        }

        ArrayList<ArrayList<Integer>> died_pieces = remove_died_pieces(test_state, piece_type == 1 ? 2 : 1);

        if (!find_liberty(test_state, i, j)){
            return false;
        }
        else if(died_pieces != null && died_pieces.size() != 0 && isBaordEqual(prevState, state)){
            return false;
        }


        return true;

    }

    public static void setInitialValues(){
        size = 5;
        moveLimit = size * size - 1;
        komi = size / 2;
        depth_limit = DEPTH_LIMIT;
        options= new int[5][2];
        options[0][0] = 2; options[0][1] = 2;
        options[1][0] = 3; options[1][1] = 1;
        options[2][0] = 1; options[2][1] = 2;
        options[3][0] = 1; options[3][1] = 3;
        options[4][0] = 3; options[4][1] = 3;
    }

    public static void main(String[] args){

        setInitialValues();
        readInput(INPUT_FILE);

        ArrayList<Integer> nextMove = move(global_previous_board, global_board, player_piece_type);
        
        if(nextMove == null || nextMove.size() == 0){
            writeOutput(OUTPUT_FILE, true, 0, 0);
        }
        else{
            writeOutput(OUTPUT_FILE, false, nextMove.get(0), nextMove.get(1));
        }

    }   

}
