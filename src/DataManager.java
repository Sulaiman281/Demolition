import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.Scanner;

public class DataManager {
    private final String config_file = "/config.json";

    public void readFile(Queue<Level> levels, Player player){

        try{
            File file = new File(getClass().getResource(config_file).toURI().getPath());
            if(!file.exists()) return;

            FileReader reader = new FileReader(file);
            JSONObject object = new JSONObject(reader);
            player.setLives(object.getInt("lives"));

            JSONArray array = object.getJSONArray("levels");
            for(int i = 0; i < array.size(); i++){
                JSONObject obj = array.getJSONObject(i);
                levels.add(loadLevel(obj.getString("path"), obj.getInt("time")));
            }
        } catch (FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Level loadLevel(String levelFile, int time){
        Level level = new Level();
        try {
            File file = new File(getClass().getResource("/"+levelFile).toURI().getPath());
            if(!file.exists()) return null;
            Scanner scanner = new Scanner(file);
            int j = 0;
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                for(int i = 0; i < line.length(); i++){
                    char ch = line.charAt(i);
                    if(ch == 'P'){
                        level.playerSX = i*App.TILE_WIDTH;
                        level.playerSY = (j+2)*App.TILE_HEIGHT;
                    }else if(ch == 'R'){
                        // spawn Red AI player at this position.
                        level.setRed(new Enemy(i* App.TILE_WIDTH, (j+2)*App.TILE_HEIGHT));
                    }else if(ch == 'Y'){
                        // spawn Yellow AI player at this position.
                        level.setYellow(new Enemy(i* App.TILE_WIDTH, (j+2)*App.TILE_HEIGHT));

                    }
                    level.getGrid()[i][j] = App.getTile(i, j+2, ch);
                }
                j++;
            }
        } catch (URISyntaxException | FileNotFoundException ignored) {
            return null;
        }
        level.setTimer(time);
        return level;
    }
}
