package myGameEngine;
import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraMoveFoward extends AbstractInputAction{
    private myGame game;
    private Camera camera;

    public CameraMoveFoward(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
    }

    public void performAction(float time, Event e){

        Vector3f v = camera.getFd();
        Vector3f p = camera.getPo();
        Vector3f p1 =
                (Vector3f) Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
        Vector3f p2 = (Vector3f) p.add(p1);

        if(camera.getMode() == 'c'){

            camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
            System.out.println("Camera has moved foward.");
        }
        else{
            SceneNode dolphinNode = game.getEngine().getSceneManager().getSceneNode("dolphinENode");
            //dolphinNode.setLocalPosition(Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
            dolphinNode.moveForward(p2.x());
            System.out.println("Camera has moved foward.");
        }
    }
}