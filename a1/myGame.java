package a1;
import myGameEngine.*;

import java.awt.*;
import java.io.*;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

public class myGame extends VariableFrameRateGame {

    // to minimize variable allocation in update()
    GL4RenderSystem rs;
    GenericInputManager im;
    float elapsTime = 0.0f, timeCounter = 0.0f;
    boolean stop = false;
    String elapsTimeStr, counterStr, dispStr;
    int elapsTimeSec, counter = 0, maxPlanets = 5, muberPoints = 0, muberTotal = 0;
    SceneNode nullNode;

    // Array to hold manual objects
    ManualObject[] muberE = new ManualObject[maxPlanets];
    ManualObject[] muberN = new ManualObject[maxPlanets];

    // Array to hold planets
    SceneNode[] planetAmount = new SceneNode[maxPlanets];
    // Array to hold planets that have been visited.
    SceneNode[] planetVisited = new SceneNode[maxPlanets];
    // Array to hold mubers that have been picked up.
    SceneNode[] mubersPicked = new SceneNode[maxPlanets];


    String[] textureFiles = {"blue.jpeg","chain-fence.jpeg", "earth-day.jpeg","earth-night.jpeg","hexagons.jpeg", "moon.jpeg", "red.jpeg"};

    public myGame() {
        super();
    }

    public static void main(String[] args) {
        Game game = new myGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        camera.setMode('c');

        camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
        camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));
    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {

        /*========= Creating a Null Test Node ==================================================== */
        nullNode = sm.getRootSceneNode().createChildSceneNode("NULL");
        /*======================================================================================== */

        /*========= Objects to set up planets ==================================================== */
        Entity[] planetE = new Entity[maxPlanets];
        int maxDistance = 20;
        int minDistance = 2;
        /*=========================================================================================*/

        /*========= Objects to set up changing textures ==================================================== */
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        TextureState state;
        /*===================================================================================================*/

        /*========= DOLPHIN and DOLPHIN CAMERA NODE ==================================================== */
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode("dolphinENode");
        dolphinN.moveBackward(2.0f);
        dolphinN.attachObject(dolphinE);

        SceneNode dolphinCN = dolphinN.createChildSceneNode("dolphinCameraNode");
        dolphinCN.setLocalPosition(0.0f, 0.5f, -0.3f);
        dolphinCN.attachObject(getEngine().getSceneManager().getCamera("MainCamera"));

        SceneNode babyCarryNode = dolphinN.createChildSceneNode("babycarry");
        babyCarryNode.setLocalPosition(0.0f, 0.05f, -0.5f);
        /*=======================================================================*/

        /*========= PLANETS ==================================================== */
        for (int i = 0; i < maxPlanets; i++){
            planetE[i] = sm.createEntity("myPlanet" + i, "earth.obj");
            planetE[i].setPrimitive(Primitive.TRIANGLES);
        }

        for (int i = 0; i < maxPlanets; i++){
            float scalePlanetNum = new Random().nextFloat() + 0.5f;
            planetAmount[i] = sm.getRootSceneNode().createChildSceneNode(planetE[i].getName() + "Node");
            planetAmount[i].moveBackward((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveForward((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveLeft((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveRight((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].scale(scalePlanetNum,scalePlanetNum,scalePlanetNum);
            planetAmount[i].attachObject(planetE[i]);
        }
        /*=======================================================================*/

        /*========= Code to spawn manual object in world ==================================================== */
        for (int i = 0; i < maxPlanets; i++){
            muberE[i] = makeDiamondEngine(eng, sm);
        }

        for (int i = 0; i < maxPlanets; i++){
            SceneNode diamondN =
                    sm.getSceneNode("myPlanet" + i + "Node").createChildSceneNode("diamondNode" + i);
            diamondN.scale(0.05f, 0.05f, 0.05f);
            diamondN.attachObject(muberE[i]);
        }
        /*=======================================================================*/

        /*========= Code to spawn XYZ Coordinates in world ==================================================== */
        SceneObject xBarE = makeXBarEngine(eng,sm);
        ((ManualObject) xBarE).setPrimitive(Primitive.LINES);
        SceneNode xBarN = sm.getRootSceneNode().createChildSceneNode("XBar");
        xBarN.scale(2.0f,2.0f,2.0f);
        xBarN.attachObject(xBarE);

        SceneObject yBarE = makeYBarEngine(eng,sm);
        ((ManualObject) yBarE).setPrimitive(Primitive.LINES);
        SceneNode yBarN = sm.getRootSceneNode().createChildSceneNode("YBar");
        yBarN.scale(2.0f,2.0f,2.0f);
        yBarN.attachObject(yBarE);

        SceneObject zBarE = makeZBarEngine(eng,sm);
        ((ManualObject) zBarE).setPrimitive(Primitive.LINES);
        SceneNode zBarN = sm.getRootSceneNode().createChildSceneNode("ZBar");
        zBarN.scale(2.0f,2.0f,2.0f);
        zBarN.attachObject(zBarE);
        /*=======================================================================*/

        /*======== LIGHTING ====================================================*/
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(5f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        /*=======================================================================*/

        /*======== ROTATION and Texture Set ====================================================*/
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
        for (int i = 0; i < maxPlanets; i++){
            state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
            rc.addNode(planetAmount[i]);
            state.setTexture(tm.getAssetByPath(textureFiles[new Random().nextInt(textureFiles.length)]));
            planetE[i].setRenderState(state);

        }
        sm.addController(rc);
        /*=======================================================================*/

        state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(tm.getAssetByPath("Dolphin_HighPolyUV_Muber.jpg"));
        dolphinE.setRenderState(state);

        // This will call a function that will create the inputs for the game.
        setupInputs();

    }

    protected void setupInputs(){
        // build some action objects for doing things in response to user input
        QuitGameAction quitGameAction = new QuitGameAction(this);
        IncrementCounterAction incrementCounterAction = new IncrementCounterAction(this);
        CameraChangeView cameraChangeView = new CameraChangeView(this);
        CameraMoveFowardBack cameraMoveFoward = new CameraMoveFowardBack(this);
        CameraMoveLeftRight cameraMoveLeftRight = new CameraMoveLeftRight(this);
        CameraTiltLeftRight cameraTiltLeftRight = new CameraTiltLeftRight(this);
        CameraTiltUpDown cameraTiltUpDown = new CameraTiltUpDown(this);
        CameraReset cameraReset = new CameraReset(this);
        CameraMoveRoll cameraMoveRoll = new CameraMoveRoll(this);

        // Creates and sets up inputs.
        im = new GenericInputManager();
        ArrayList controllers = im.getControllers();
        for (int i = 0; i < controllers.size(); i++) {
            Controller c = (Controller)controllers.get(i);
            if (c.getType() == Controller.Type.KEYBOARD) {
                im.associateAction(
                        c,
                        Component.Identifier.Key.W,
                        new CameraMoveFowardBack(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.S,
                        new CameraMoveFowardBack(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.D,
                        new CameraMoveLeftRight(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.A,
                        new CameraMoveLeftRight(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.UP,
                        new CameraTiltUpDown(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.DOWN,
                        new CameraTiltUpDown(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.LEFT,
                        new CameraTiltLeftRight(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.RIGHT,
                        new CameraTiltLeftRight(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.SPACE,
                        new CameraChangeView(this),
                        InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

                im.associateAction(
                        c,
                        Component.Identifier.Key.R,
                        new CameraReset(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.E,
                        new CameraMoveRoll(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.Q,
                        new CameraMoveRoll(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            }
        else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {

            im.associateAction(c,
                    net.java.games.input.Component.Identifier.Button._9,
                    quitGameAction,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(c,
                    net.java.games.input.Component.Identifier.Button._3,
                    cameraReset,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(c,
                    net.java.games.input.Component.Identifier.Button._1,
                    cameraChangeView,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(c,
                    Component.Identifier.Axis.Y,
                    cameraMoveFoward,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(c,
                    Component.Identifier.Axis.X,
                    cameraMoveLeftRight,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(c,
                    Component.Identifier.Axis.RX,
                    cameraTiltLeftRight,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(c,
                    Component.Identifier.Axis.RY,
                    cameraTiltUpDown,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(c,
                    Component.Identifier.Axis.Z,
                    cameraMoveRoll,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
         }
        }
    }

    //============== UPDATE ==========================================================================
    @Override
    protected void update(Engine engine) {
        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();
        elapsTime += engine.getElapsedTimeMillis();
        elapsTimeSec = Math.round(elapsTime/1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);
        counterStr = Integer.toString(counter);
        im.update(elapsTime);
        checkDistance();
        muberGame(elapsTimeSec);
        dispStr = hudContent("Time = " + elapsTimeSec +"  Visited Planets = " + counterStr + "   Muber Points = " + muberTotal );
        rs.setHUD(dispStr, 13, 13);
    }
    //==============================================================================================

    // ======== This will update the HUD when the player gets too far from the dolphin. ===============
    private String hudContent(String display){
        String content = display;
        if(stop && getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'c'){
            content = "  Too far from dolphin, Press B(Controller) or SpaceBar(Keyboard)." ;
        }else if (muberPoints != 0){
            content = content + "     Nice drop off, you just got rated a " +  muberPoints + " out of 5!";
        }
        return content;
    }
    //==========================================================================================

    // ======== This will check the distant between the player and the Dolphin. ===============
    public boolean dolphinDistanceLimit(){
        boolean limit = true;
        float distanceLimit = 3.0f;
        float distanceX, distanceZ;
        SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinENode");
        Vector3 cameraPosition = getEngine().getSceneManager().getCamera("MainCamera").getPo();

        distanceX = Math.abs(dolphin.getLocalPosition().x() - cameraPosition.x());
        distanceZ = Math.abs(dolphin.getLocalPosition().z() - cameraPosition.z());

        if (distanceX > distanceLimit && distanceZ > distanceLimit){
            limit = false;
            stop = true;
        }else {
            stop = false;
        }

        return limit;
    }
    //==========================================================================================

    //======== This will check the distant between the player and the planets ================
    private void checkDistance(){
        if(getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'c'){
            Vector3 planetPosition;
            float distanceX, distanceZ, distantLimit = 1.5f;
            Camera camera = getEngine().getSceneManager().getCamera("MainCamera");

            for (int i = 0; i < maxPlanets; i++){
                planetPosition = planetAmount[i].getLocalPosition();
                if (!(visitYet(planetAmount[i]))){
                    distanceX = Math.abs(camera.getPo().x() - planetPosition.x());
                    distanceZ = Math.abs(camera.getPo().z() - planetPosition.z());
                    if(distanceX < distantLimit && distanceZ < distantLimit){
                        planetVisited[i] = planetAmount[i];
                        incrementCounter();
                        }

                    }
                }
            }
    }
    //==========================================================================================

    //======== This will check if a planet has been visited yet =======================
    private boolean visitYet(SceneNode nodePlanet){
        boolean isIn = false;
        for (int i=0; i < maxPlanets; i++){
            if (nodePlanet == planetVisited[i]){
                isIn = true;
            }
        }
        return isIn;
    }
    //==========================================================================================

    //=========== This will create X Cordanate Bar as a manual object =========================
    private ManualObject makeXBarEngine (Engine eng, SceneManager sm) throws IOException {
        ManualObject xBar = sm.createManualObject("XBar");
        ManualObjectSection xBarSec = xBar.createManualSection("SquareSection");
        xBar.setGpuShaderProgram(sm.getRenderSystem().
                getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]{
                // X Plane
                1f,0f,0f,0f,0f,0f,
                0f,0f,0f,1f,0f,0f

        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        float[] normals = new float[]{
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f

        };

        int[] indices = new int[] {0,1,2,3};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        xBarSec.setVertexBuffer(vertBuf);
        xBarSec.setTextureCoordsBuffer(texBuf);
        xBarSec.setNormalsBuffer(normBuf);
        xBarSec.setIndexBuffer(indexBuf);

        Texture tex =
                eng.getTextureManager().getAssetByPath("bright-red.jpeg");
        TextureState texState = (TextureState)sm.getRenderSystem().
                createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
                createRenderState(RenderState.Type.FRONT_FACE);

        xBar.setDataSource(DataSource.INDEX_BUFFER);
        xBar.setRenderState(texState);
        xBar.setRenderState(faceState);

        return xBar;
    }
    //==========================================================================================

    //=========== This will create Y Cordanate Bar as a manual object =========================
    private ManualObject makeYBarEngine (Engine eng, SceneManager sm) throws IOException {
        ManualObject yBar = sm.createManualObject("YBar");
        ManualObjectSection yBarSec = yBar.createManualSection("SquareSection");
        yBar.setGpuShaderProgram(sm.getRenderSystem().
                getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]{
                // Y Plane
                0f,1f,0f,0f,0f,0f,
                0f,0f,0f,0f,1f,0f

        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        float[] normals = new float[]{
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f

        };

        int[] indices = new int[] {0,1,2,3};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        yBarSec.setVertexBuffer(vertBuf);
        yBarSec.setTextureCoordsBuffer(texBuf);
        yBarSec.setNormalsBuffer(normBuf);
        yBarSec.setIndexBuffer(indexBuf);

        Texture tex =
                eng.getTextureManager().getAssetByPath("default.png");
        TextureState texState = (TextureState)sm.getRenderSystem().
                createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
                createRenderState(RenderState.Type.FRONT_FACE);

        yBar.setDataSource(DataSource.INDEX_BUFFER);
        yBar.setRenderState(texState);
        yBar.setRenderState(faceState);

        return yBar;
    }
    //==========================================================================================

    //=========== This will create Z Cordanate Bar as a manual object =========================
    private ManualObject makeZBarEngine (Engine eng, SceneManager sm) throws IOException {
        ManualObject zBar = sm.createManualObject("ZBar");
        ManualObjectSection zBarSec = zBar.createManualSection("SquareSection");
        zBar.setGpuShaderProgram(sm.getRenderSystem().
                getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]{
                // Z Plane
                0f,0f,1f,0f,0f,0f,
                0f,0f,0f,0f,0f,1f

        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        float[] normals = new float[]{
                1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f

        };

        int[] indices = new int[] {0,1,2,3};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        zBarSec.setVertexBuffer(vertBuf);
        zBarSec.setTextureCoordsBuffer(texBuf);
        zBarSec.setNormalsBuffer(normBuf);
        zBarSec.setIndexBuffer(indexBuf);

        Texture tex =
                eng.getTextureManager().getAssetByPath("bright-green.jpeg");
        TextureState texState = (TextureState)sm.getRenderSystem().
                createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
                createRenderState(RenderState.Type.FRONT_FACE);

        zBar.setDataSource(DataSource.INDEX_BUFFER);
        zBar.setRenderState(texState);
        zBar.setRenderState(faceState);

        return zBar;
    }
    //==========================================================================================

    //=========== This will create manual objects with names from 0 to 5 =======================
    private ManualObject makeDiamondEngine (Engine eng, SceneManager sm) throws IOException {
        ManualObject diamondShape = sm.createManualObject(new Random(maxPlanets).toString());
        ManualObjectSection diamondShapeSec = diamondShape.createManualSection("SquareSection");
        diamondShape.setGpuShaderProgram(sm.getRenderSystem().
                getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]{
                -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Bot front
                1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // Bot right
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // Bot back
                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Bot left
                -1.0f, 2.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 2.0f, -1.0f, // Top Left
                1.0f, 2.0f, -1.0f,1.0f, 1.0f, 1.0f,1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 2.0f, -1.0f, // Top Right
                -1.0f, 1.0f, 1.0f,1.0f, 1.0f, 1.0f,-1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, //UF
                1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f //UR

        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f,0.0f, 0.0f, 1.0f,
        };

        float[] normals = new float[]{
                0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };

        int[] indices = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        diamondShapeSec.setVertexBuffer(vertBuf);
        diamondShapeSec.setTextureCoordsBuffer(texBuf);
        diamondShapeSec.setNormalsBuffer(normBuf);
        diamondShapeSec.setIndexBuffer(indexBuf);

        Texture tex =
                eng.getTextureManager().getAssetByPath(textureFiles[new Random().nextInt(textureFiles.length)]);
        TextureState texState = (TextureState)sm.getRenderSystem().
                createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
                createRenderState(RenderState.Type.FRONT_FACE);

        diamondShape.setDataSource(DataSource.INDEX_BUFFER);
        diamondShape.setRenderState(texState);
        diamondShape.setRenderState(faceState);

        return diamondShape;
    }
    //==========================================================================================

    //============ My Muber Game Logic =========================================================
    private void muberGame(float time){

        if(getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'c'){
            float timeGame = time;
            float distanceLimit = 1.0f;
            float distanceX, distanceZ, distanceY;
            Vector3 cameraPosition = getEngine().getSceneManager().getCamera("MainCamera").getPo();

            for(int i = 0; i < maxPlanets; i++){
                if(getEngine().getSceneManager().getSceneNode("babycarry").getAttachedObjectCount() != 1){

                        SceneNode muberNode = getEngine().getSceneManager().getSceneNode("diamondNode" + i);
                        distanceX = Math.abs(cameraPosition.x() - muberNode.getLocalPosition().x());
                        distanceY = Math.abs(cameraPosition.y() - muberNode.getLocalPosition().y());
                        distanceZ = Math.abs(cameraPosition.z() - muberNode.getLocalPosition().z());

//                        if (distanceX < distanceLimit && distanceZ < distanceLimit && distanceY < distanceLimit){
                            if (!isPickedUp(getEngine().getSceneManager().getSceneNode("myPlanet" + i + "Node"))) {
                                mubersPicked[i] = getEngine().getSceneManager().getSceneNode("myPlanet" + i + "Node");
                                getEngine().getSceneManager().getSceneNode("babycarry").attachObject(muberNode.getAttachedObject(0));
                                nullNode = getEngine().getSceneManager().getSceneNode("myPlanet" + i + "Node");
                                getEngine().getSceneManager().getSceneNode("myPlanet" + i + "Node").detachAllChildren();
                                if(getEngine().getSceneManager().getSceneNode("babycarry").getLocalScale().x() == 1 )
                                    getEngine().getSceneManager().getSceneNode("babycarry").scale(.05f,.05f,.05f);
//                            }
                        }

                } else if(getEngine().getSceneManager().getSceneNode("babycarry").getAttachedObjectCount() == 1){
                    Vector3 planetPosition;
                    float distantLimit = 1.5f;
                    Camera camera = getEngine().getSceneManager().getCamera("MainCamera");

                    for (int k = 0; k < maxPlanets; k ++){
                        planetPosition = planetAmount[k].getLocalPosition();
                            distanceX = Math.abs(camera.getPo().x() - planetPosition.x());
                            distanceZ = Math.abs(camera.getPo().z() - planetPosition.z());

                            if(distanceX < distantLimit && distanceZ < distantLimit && planetAmount[k] == nullNode){
                                calculatePoints(timeGame);
                                timeCounter = timeGame;
                                getEngine().getSceneManager().getSceneNode("babycarry").detachAllObjects();
                            }
                    }
                }
            }

        }
    }

    private boolean isPickedUp(SceneNode muber){
        boolean pickedUp = false;
        for (int i=0; i < maxPlanets; i++){
            if (muber == mubersPicked[i]){
                pickedUp = true;
            }
        }
        return pickedUp;
    }

    private void calculatePoints(float time){
        float currentTime = time;
        if(currentTime - timeCounter <= 10){
            muberPoints = 5;
            muberTotal = muberPoints + muberTotal;
        } else if (currentTime - timeCounter <= 20){
            muberPoints = 3;
            muberTotal = muberPoints + muberTotal;
        } else if (currentTime - timeCounter > 20){
            muberPoints = 1;
            muberTotal = muberPoints + muberTotal;
        }

    }

    //=============== Game Logic Ends ====================================================

    public void incrementCounter() {
        counter++;
    }


}
