package myGameEngine.Camera;
import a1.*;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraTiltUpDown extends AbstractInputAction {

    private myGame game;
    private Camera camera;
    private SceneNode dolphinCamNode;

    public CameraTiltUpDown(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
//        dolphinCamNode = g.getEngine().getSceneManager().getSceneNode("dolphinCameraNode");
    }

    @Override
    public void performAction(float v, Event event) {

        Vector3 fd = camera.getFd();
        Vector3 rt = camera.getRt();
        Vector3 up = camera.getUp();
        float tilt;

        float degreeAmount = .7f;
        Angle rotAmt = Degreef.createFrom(degreeAmount);

        if(event.getValue() < -0.1 || event.getComponent().getName().equals("Up")){
                        rotAmt = Degreef.createFrom(-degreeAmount);

            if (camera.getMode() == 'c'){
                tilt = 1f;
                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), rt)).normalize();
                Vector3 upFinal = (up.rotate(Degreef.createFrom(degreeAmount * tilt), rt)).normalize();
                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
                camera.setUp((Vector3f)Vector3f.createFrom(upFinal.x(),upFinal.y(),upFinal.z()));

            }else{
                dolphinCamNode.pitch(rotAmt);
            }

        }else if (event.getValue() > 0.1 || event.getComponent().getName().equals("Down")){

            if (camera.getMode() == 'c'){
                tilt = -1f;
                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), rt)).normalize();
                Vector3 upFinal = (up.rotate(Degreef.createFrom(degreeAmount * tilt), rt)).normalize();
                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
                camera.setUp((Vector3f)Vector3f.createFrom(upFinal.x(),upFinal.y(),upFinal.z()));
            }else{
                dolphinCamNode.pitch(rotAmt);
            }

        }

    }
}
