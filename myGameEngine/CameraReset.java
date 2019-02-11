package myGameEngine;
import a1.*;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraReset extends AbstractInputAction {

    private myGame game;
    private Camera camera;
    private SceneNode dolphinCamNode;

    public CameraReset(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinCamNode = g.getEngine().getSceneManager().getSceneNode("dolphinCameraNode");
    }

    @Override
    public void performAction(float v, Event event) {

        Vector3 fd = camera.getFd();
        Vector3 rt = camera.getRt();
        Vector3 up = camera.getUp();

        if(camera.getMode() == 'c'){

            camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
            camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
            camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));

        }else{
        }

//        if(event.getValue() < -0.1){
//            if (camera.getMode() == 'c'){
//                tilt = 1f;
//                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
//                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
//                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
//                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));
//
//            }else{
//                dolphinCamNode.yaw(rotAmt);
//            }
//
//        }else if (event.getValue() > 0.1){
//            rotAmt = Degreef.createFrom(-degreeAmount);
//
//            if (camera.getMode() == 'c'){
//                tilt = -1f;
//                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
//                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
//                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
//                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));
//            }else{
//                dolphinCamNode.yaw(rotAmt);
//            }
//
//        }

    }
}
