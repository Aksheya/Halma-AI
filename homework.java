import java.net.URISyntaxException;
import java.util.*;
import java.io.*;
import java.util.Arrays;

class Pair
{
    // Return a map entry (key-value pair) from the specified values
    public static <T, U> Map.Entry<T, U> of(T first, U second)
    {
        return new AbstractMap.SimpleEntry<>(first, second);
    }
}
class DataObject{
    Map.Entry<Integer, Integer> source;
    LinkedList<Map.Entry<Integer, Integer>> destination;
    Map<String,String> path_map;
    DataObject(Map.Entry<Integer, Integer>source, LinkedList<Map.Entry<Integer, Integer>> destination, Map<String,String> path_map){
        this.source = source;
        this.destination = destination;
        this.path_map = path_map;
    }
}
class Node{
    int pawn;
    int camp;
    Node(int pawn, int camp){
        this.pawn = pawn;
        this.camp = camp;
    }
}
class Move{
    double val;
    Map.Entry<Integer, Integer> move;
    boolean win;
    Move(double val, boolean win){
        this.val = val;
        this.win = win;
    }
}
public class homework{
    private static int player;
    private static Node[][] grid;
    boolean winning = false;
    private static String game_type;
    private int[] white_rows = new int[]{-1,0,-1,1,-1,1,0,1};
    private int[] white_cols = new int[]{-1,-1,0,-1,1,0,1,1};
    private int[] black_rows = new int[]{1,0,1,-1,1,-1,0,-1};
    private int[] black_cols = new int[]{1,1,0,1,-1,0,-1,0,-1,-1};
    private Map.Entry<Map.Entry<Integer, Integer>,Map.Entry<Integer, Integer>>best_move;
    private Map<Map.Entry<Integer, Integer>,Map<Map.Entry<Integer, Integer>,Map.Entry<Integer, Integer>>> path_map = new HashMap<>();
    List<Map.Entry<Integer, Integer>> goal_for_black  = new ArrayList<>();
    List<Map.Entry<Integer, Integer>> goal_for_white = new ArrayList<>();
    private Map<String,Map<String,String>> path = new HashMap<>();

    private int winningCondition(){
        int white=0;
        int black=0;
        for(Map.Entry<Integer, Integer> pair : goal_for_white){
            int value = grid[pair.getKey()][pair.getValue()].pawn;
            if(value == 1)
                white++;
            else if(value == 2)
                black++;
        }
        if( white>0  && (white==19 || white+black==19)){
            return 1;
        }
        white = 0;
        black = 0;
        for(Map.Entry<Integer, Integer> pair: goal_for_black){
            int value = grid[pair.getKey()][pair.getValue()].pawn;
            if(value==1)
                white++;
            else if(value==2)
                black++;
        }
        if(black>0 && (black==19 || white+black==19))
            return 2;
        return 0;
    }

    private double heuristic(int x1,int y1,Map.Entry<Integer, Integer>pair){
        int x2 = pair.getKey();
        int y2 = pair.getValue();
        return Math.sqrt((y1-y2)*(y1-y2) + (x1-x2)*(x1-x2));
    }

    private double eval_func(int m_player){
        double utility_value=0;
        for(int i=0;i<grid.length;i++){
            for(int j=0;j<grid[0].length;j++){
                // maximize
                if(grid[i][j].pawn == player){
                    List<Map.Entry<Integer, Integer>> goalList;
                    if(player == 1) goalList = goal_for_white;
                    else goalList = goal_for_black;
                    List<Double> dist = new ArrayList<>();
                    for(Map.Entry<Integer, Integer> pair:goalList){
                        if(grid[pair.getKey()][pair.getValue()].pawn!=player)
                            dist.add(heuristic(i,j, pair));
                    }
                    // TODO : hardcode for initial config
                    utility_value  -= dist.size()>0 ? Collections.max(dist) : -50;
                }
                else if(grid[i][j].pawn !=player && grid[i][j].pawn!=0){
                    List<Map.Entry<Integer, Integer>> goalList;
                    if(grid[i][j].pawn == 1) goalList = goal_for_white;
                    else goalList = goal_for_black;
                    List<Double> dist = new ArrayList<>();
                    for(Map.Entry<Integer, Integer> pair:goalList){
                        if(grid[pair.getKey()][pair.getValue()].pawn!=player)
                            dist.add(heuristic(i,j, pair));
                    }
                    // TODO : hardcode for initial config
                    utility_value  += dist.size()>0 ? Collections.max(dist) : -50;
                }
            }
        }
        return utility_value;
    }


    private Integer[] removeElement(Integer[] arr, int i){
        List<Integer> tempList = new ArrayList<>(Arrays.asList(arr));
        tempList.remove(i);
        return tempList.toArray(new Integer[0]);
    }
    private DataObject getDestinationNodes(boolean jump,int player_color,Map.Entry<Integer, Integer> start, DataObject dobj, Map.Entry<Integer, Integer> main_start, boolean first,boolean single){
        int enemy_color = player_color==1?2:1;
        Integer[] allowed_nodes = new Integer[]{0,1,2};
        if(grid[start.getKey()][start.getValue()].camp!=player_color){
            for(int i=0;i<allowed_nodes.length;i++){
                if(allowed_nodes[i]==player_color){
                    allowed_nodes = removeElement(allowed_nodes,i);
                    break;
                }
            }
        }
        if(grid[start.getKey()][start.getValue()].camp == enemy_color){
            for(int i=0;i<allowed_nodes.length;i++){
                if(allowed_nodes[i]==0){
                    allowed_nodes = removeElement(allowed_nodes,i);
                    break;
                }
            }
        }
        List<Integer> list = Arrays.asList(allowed_nodes);
        int[] rows;
        int[] cols;
        if(player_color==1){
            rows = white_rows;
            cols = white_cols;
        }
        else{
            rows  = black_rows;
            cols = black_cols;
        }
        List<Map.Entry<Integer,Integer>> illegal_white_moves = new ArrayList<>();
        illegal_white_moves.add(Pair.of(start.getKey(),start.getValue()+1));
        illegal_white_moves.add(Pair.of(start.getKey()+1,start.getValue()+1));
        illegal_white_moves.add(Pair.of(start.getKey()+1,start.getValue()));
        illegal_white_moves.add(Pair.of(start.getKey()-1,start.getValue()+1));
        illegal_white_moves.add(Pair.of(start.getKey()+1,start.getValue()-1));
        List<Map.Entry<Integer,Integer>> illegal_black_moves = new ArrayList<>();
        illegal_black_moves.add(Pair.of(start.getKey()-1,start.getValue()));
        illegal_black_moves.add(Pair.of(start.getKey()-1,start.getValue()-1));
        illegal_black_moves.add(Pair.of(start.getKey(),start.getValue()-1));
        illegal_black_moves.add(Pair.of(start.getKey()-1,start.getValue()+1));
        illegal_black_moves.add(Pair.of(start.getKey()+1,start.getValue()-1));
        for(int i=0;i<rows.length;i++){
            int x = start.getKey()+rows[i];
            int y = start.getValue()+cols[i];

            if(x>=0 && x<grid.length && y>=0 && y<grid[0].length){
                if(!list.contains(grid[x][y].camp))
                    continue;
                if(grid[start.getKey()][start.getValue()].camp == player_color){
                    if(player_color == 1){
                        if( illegal_white_moves.contains(Pair.of(x,y)))
                            continue;
                    }
                    else if(player_color==2){
                        if(illegal_black_moves.contains(Pair.of(x,y)))
                            continue;
                    }
                }
                if(grid[x][y].pawn==0){
                    if(jump)
                        continue;
                    else{
                        dobj.destination.add(Pair.of(x,y));
                        if(first)
                            this.path.get(main_start.getKey() + "," + main_start.getValue()).put(x+","+y,start.getKey() + ","+start.getValue());
                        if(single)
                            return dobj;
                        continue;
                    }
                }
                x +=rows[i];
                y+=cols[i];
                if(x>=0 && x<grid.length && y>=0 && y<grid[0].length){
                    Map.Entry<Integer, Integer> pair = Pair.of(x,y);
                    if((dobj.destination.contains(pair)) ||  !list.contains(grid[x][y].camp))
                        continue;
                    if(grid[x][y].pawn == 0){
                        if(dobj.destination.size()>0)
                            dobj.destination.addFirst(Pair.of(x,y));
                        else
                            dobj.destination.add(Pair.of(x,y));
                        if(first)
                            this.path.get(main_start.getKey() + "," + main_start.getValue()).put(x+","+y,start.getKey() + ","+start.getValue());
                        if(single)
                            return dobj;

                        DataObject d = getDestinationNodes(true,player_color,Pair.of(x,y),dobj,main_start,first,single);
                    }
                }
            }
        }
        return dobj;
    }

    private  Move maxValue(int depth, double alpha, double beta, int current_player, double time_left, boolean first){
        boolean win;
        int enemy_player = current_player == 1? 2 : 1;
        if(winningCondition()>0) {
            win = true;
            winning = true;
        }
        else win = false;
        if (depth==0 || win || (time_left < System.currentTimeMillis()/1000.0))
            return new Move(eval_func(current_player),win);
        double best_value = Integer.MIN_VALUE;

        List<DataObject> movesss = new ArrayList<DataObject>();
        List<Map.Entry<Integer, Integer>> camp_pawns = new ArrayList<>();
        List<Map.Entry<Integer, Integer>> enemy_camp_pawns = new ArrayList<>();
        List<Map.Entry<Integer, Integer>> non_camp_pawns = new ArrayList<>();
        for(int i=0;i<grid.length;i++){
            for(int j=0;j<grid[0].length;j++){
                if(grid[i][j].pawn != current_player)
                    continue;
                Map.Entry<Integer, Integer> start = Pair.of(i,j);
                if(grid[i][j].camp == current_player)
                    camp_pawns.add(start);
                else if(grid[i][j].camp == enemy_player)
                    enemy_camp_pawns.add(start);
                else
                    non_camp_pawns.add(start);
            }
        }
        if(camp_pawns.size()>0){
            List<DataObject> outside_list = new ArrayList<>();
            List<DataObject> inside_list = new ArrayList<>();
            for(Map.Entry<Integer, Integer> start : camp_pawns){
                if(first)
                    this.path.put(start.getKey() + ","+start.getValue(),new HashMap<>());
                DataObject endList = getDestinationNodes(false,current_player,start, new DataObject(start,new LinkedList<>(),new HashMap<String,String>()), start,first,false);

                if(endList.destination.size()>0){
                    //movesss.add(endList);
                    LinkedList<Map.Entry<Integer,Integer>> outside = new LinkedList<>();
                    for(int i = 0;i<endList.destination.size();i++){
                        if(grid[endList.destination.get(i).getKey()][endList.destination.get(i).getValue()].camp !=current_player)
                            outside.add(endList.destination.get(i));
                    }
                    if(outside.size()>0){
                        endList.destination = outside;
                        outside_list.add(endList);
                    }
                    else
                        inside_list.add(endList);
                }
            }
            if(outside_list.size()>0){
                for(DataObject dObj : outside_list){
                    movesss.add(dObj);
                }
            }
            else if(inside_list.size()>0){
                for(DataObject dObj : inside_list){
                    movesss.add(dObj);
                }
            }
        }
        if(non_camp_pawns.size()>0 && movesss.size()==0){
            for(Map.Entry<Integer, Integer> start : non_camp_pawns){
                if(first)
                    this.path.put(start.getKey() + ","+start.getValue(),new HashMap<>());
                DataObject endList = getDestinationNodes(false,current_player,start, new DataObject(start,new LinkedList<>(),new HashMap<String,String>()),start,first,false);
                if(endList.destination.size()>0){
                    movesss.add(endList);
                }
            }
        }
        if(movesss.size()==0 && enemy_camp_pawns.size()>0){
            for(Map.Entry<Integer, Integer> start : enemy_camp_pawns){
                if(first)
                    this.path.put(start.getKey() + ","+start.getValue(),new HashMap<>());
                DataObject endList = getDestinationNodes(false,current_player,start, new DataObject(start,new LinkedList<>(),new HashMap<String,String>()),start,first,false);
                if(endList.destination.size()>0){
                    movesss.add(endList);
                }
            }
        }

        for( DataObject dObj : movesss){
            Map.Entry<Integer, Integer> source = dObj.source;
            for(Map.Entry<Integer, Integer> destination: dObj.destination){

                // make move
                grid[source.getKey()][source.getValue()].pawn = 0;
                grid[destination.getKey()][destination.getValue()].pawn = current_player;
                if(this.best_move == null){
                    this.best_move = Pair.of(source,destination);
                }
                Move move = minValue(depth-1,alpha,beta,current_player==1?2:1, time_left);
                if(best_value<move.val) {
                    best_value = move.val;
                    if(first)
                    {
                        this.best_move = Pair.of(source, destination);

                    }

                }

                // revert move
                grid[source.getKey()][source.getValue()].pawn = current_player;
                grid[destination.getKey()][destination.getValue()].pawn = 0;

                //prune
                if(best_value>=beta){
                    return new Move(best_value,move.win);
                }
                alpha = Math.max(alpha,best_value);
                if(move.win) {return new Move(best_value,true);}
            }
        }
        return new Move(best_value,false);
    }

    private  Move minValue(int depth, double alpha, double beta, int current_player, double time_left){
        boolean win = winningCondition() > 0;
        int enemy_player = current_player == 1 ? 2 : 1;
        if (depth==0 || win || (time_left < System.currentTimeMillis()/1000.0)){
            return new Move(eval_func(current_player),win);
        }
        double best_value = Integer.MAX_VALUE;
        List<DataObject> movesss = new ArrayList<DataObject>();
        List<Map.Entry<Integer, Integer>> camp_pawns = new ArrayList<>();
        List<Map.Entry<Integer, Integer>> non_camp_pawns = new ArrayList<>();
        List<Map.Entry<Integer, Integer>> eneny_camp_pawns = new ArrayList<>();
        for(int i=0;i<grid.length;i++){
            for(int j=0;j<grid[0].length;j++){
                if(grid[i][j].pawn != current_player)
                    continue;
                Map.Entry<Integer, Integer> start = Pair.of(i,j);
                if(grid[i][j].camp == current_player)
                    camp_pawns.add(start);
                else if(grid[i][j].camp == enemy_player)
                    eneny_camp_pawns.add(start);
                else
                    non_camp_pawns.add(start);
            }
        }
        if(camp_pawns.size()>0){
            List<DataObject> outside_list = new ArrayList<>();
            List<DataObject> inside_list = new ArrayList<>();
            for(Map.Entry<Integer, Integer> start : camp_pawns) {
                DataObject endList = getDestinationNodes(false, current_player, start, new DataObject(start, new LinkedList<>(), new HashMap<String, String>()), start, false, false);
                if (endList.destination.size() > 0) {
                    LinkedList<Map.Entry<Integer,Integer>> outside = new LinkedList<>();
                    for(int i = 0;i<endList.destination.size();i++){
                        if(grid[endList.destination.get(i).getKey()][endList.destination.get(i).getValue()].camp !=current_player)
                            outside.add(endList.destination.get(i));
                    }
                    if(outside.size()>0){
                        endList.destination = outside;
                        outside_list.add(endList);
                    }
                    else
                        inside_list.add(endList);
//                    movesss.add(endList);
                }
            }
            if(outside_list.size()>0){
                for(DataObject dObj : outside_list){
                    movesss.add(dObj);
                }
            }
            else if(inside_list.size()>0){
                for(DataObject dObj : inside_list){
                    movesss.add(dObj);
                }
            }
        }
        if(non_camp_pawns.size()>0 && movesss.size()==0){
            for(Map.Entry<Integer, Integer> start : non_camp_pawns){
                DataObject endList = getDestinationNodes(false,current_player,start,new DataObject(start,new LinkedList<>(),new HashMap<String,String>()),start,false,false);
                if(endList.destination.size()>0){
                    movesss.add(endList);
                }
            }
        }
        if(movesss.size()==0 && eneny_camp_pawns.size()>0){
            for(Map.Entry<Integer, Integer> start : eneny_camp_pawns){
                DataObject endList = getDestinationNodes(false,current_player,start,new DataObject(start,new LinkedList<>(),new HashMap<String,String>()),start,false,false);
                if(endList.destination.size()>0){
                    movesss.add(endList);
                }
            }
        }
        for( DataObject dObj: movesss){
            Map.Entry<Integer, Integer> source = dObj.source;
            for(Map.Entry<Integer, Integer> destination: dObj.destination){

                // make move
                grid[source.getKey()][source.getValue()].pawn = 0;
                grid[destination.getKey()][destination.getValue()].pawn = current_player;
                Move move = maxValue(depth-1,alpha,beta,current_player==1?2:1, time_left,false);
                if(move.val<best_value) {
                    best_value = move.val;
                }

                // revert move
                grid[source.getKey()][source.getValue()].pawn = current_player;
                grid[destination.getKey()][destination.getValue()].pawn = 0;

                //prune
                if(best_value<=alpha){
                    return new Move(best_value,move.win);
                }
                beta = Math.min(beta,best_value);
                if(move.win) return new Move(best_value,true);
            }
        }
        return new Move(best_value,false);
    }

    private Move minimax(int depth, double time_left){
        return maxValue(depth, Integer.MIN_VALUE, Integer.MAX_VALUE,player, time_left,true);
    }

    private LinkedList<String> calculate_path(Map.Entry<Integer,Integer> start, Map.Entry<Integer,Integer> destination,Map<String,String> path_map){
        LinkedList<String> final_path = new LinkedList<>();
        if(path_map.size()>0){
            String child = path_map.get(destination.getKey()+","+destination.getValue());
            String[] child_int  = child.split(",");
            if(Math.abs(Integer.parseInt(child_int[0]) - destination.getKey())<=1 &&  Math.abs(Integer.parseInt(child_int[1]) - destination.getValue())<=1)
                final_path.add("E " + child_int[1] + "," + child_int[0]+ " " + destination.getValue() + "," + destination.getKey());
            else
                final_path.add("J " + child_int[1] + "," + child_int[0]+ " " + destination.getValue() + "," + destination.getKey());
            while(!child.equals(start.getKey()+","+start.getValue())){
                String[] dest  = child.split(",");
                child = path_map.get(child);
                child_int = child.split(",");
                final_path.addFirst("J " + child_int[1] + "," + child_int[0]+ " " + dest[1] + "," + dest[0]);
            }
        }
        return final_path;
    }
    public static void main(String[] args) throws  URISyntaxException {
        homework  obj1 = new homework();
        File file = new File("C:\\Users\\akshe\\Documents\\AI\\homework\\src\\input.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String str;
        List <String> l = new ArrayList<>();
        try {
            while ((str = br.readLine()) != null)
                l.add(str);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Iterator<String> itr = l.iterator();
        game_type = itr.next();
        player = itr.next().equalsIgnoreCase("WHITE")  ? 1 : 2;
        double max_time = Double.parseDouble(itr.next());
        grid = new Node[16][16];
        String pieces = "";
        int r = 0;
        int pawn = 0;
        int camp = 0;
        while(itr.hasNext()) {
            pieces = itr.next();
            int size = pieces.length();
            for (int i = 0; i < size; i++) {
                if (pieces.charAt(i) == 'W' || pieces.charAt(i) == 'w') pawn = 1;
                else if (pieces.charAt(i) == 'B' || pieces.charAt(i) == 'b') pawn = 2;
                else if (pieces.charAt(i) == '.') pawn = 0;
                else {
                    return;
                }
                if(r + i <=5){
                    if(r!=5 && i!=5) camp = 2;
                    else camp = 0;
                }
                else if(r+i>=25 && r+i<=30){
                    if(r!=10 && i!=10) camp = 1;
                    else camp = 0;
                }
                else camp = 0;
                grid[r][i] = new Node(pawn,camp);
                if(grid[r][i].camp == 1) obj1.goal_for_black.add(Pair.of(r,i));
                else if(grid[r][i].camp == 2) obj1.goal_for_white.add(Pair.of(r,i));
            }
            r++;
        }
        double time_left = System.currentTimeMillis()/1000.0 + max_time;
        if(game_type.equals("SINGLE")){
            Move move = obj1.minimax(1,time_left);
            }
        else {
            Move move;
            if(max_time<30)
                 move = obj1.minimax(3,time_left);
            else move = obj1.minimax(4, time_left);
            }
            LinkedList<String> final_path = new LinkedList<>();
            if(obj1.best_move!=null){
                Map.Entry<Integer, Integer>start = obj1.best_move.getKey();
                Map.Entry<Integer, Integer>destination = obj1.best_move.getValue();
                Map<String,String> path_map = obj1.path.get(start.getKey()+","+start.getValue());
                final_path = obj1.calculate_path(start,destination,path_map);

            }
            try {
                FileWriter fw=new FileWriter("src\\output.txt");
                for(int i=0;i<final_path.size();i++) {
                    for (int j = 0; j < final_path.get(i).length(); j++)
                        fw.write(final_path.get(i).charAt(j));
                    if(i<final_path.size()-1)
                        fw.write("\n");
                    }
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}


































