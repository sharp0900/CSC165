package a1;
import myGameEngine.*;

import java.awt.*;
import java.io.*;

import net.java.games.input.Component;
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
import java.util.Random;

public class myGame extends VariableFrameRateGame {

    // to minimize variable allocation in update()
    GL4RenderSystem rs;
    GenericInputManager im;
    float elapsTime = 0.0f;
    boolean stop = false;
    String elapsTimeStr, counterStr, dispStr;
    int elapsTimeSec, counter = 0, maxPlanets = 5;

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
        System.out.println("press T to render triangles");
        System.out.println("press L to render lines");
        System.out.println("press P to render points");
        System.out.println("press C to increment counter");
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


        /*========= Objects to set up planets ==================================================== */
        Entity[] planetE = new Entity[maxPlanets];
        int maxDistance = 20;
        int minDistance = 2;
        /*=======================================================================*/

        /*========= Objects to set up changing textures ==================================================== */
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        TextureState state;
        /*=======================================================================*/

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
        String kbName = im.getKeyboardName();
        String gpName;
        System.out.println(kbName);
        try{
            gpName = im.getFirstGamepadName();
            System.out.println(gpName);

            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._9,
                    quitGameAction,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._3,
                    cameraReset,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._1,
                    cameraChangeView,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    Component.Identifier.Axis.Y,
                    cameraMoveFoward,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gpName,
                    Component.Identifier.Axis.X,
                    cameraMoveLeftRight,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gpName,
                    Component.Identifier.Axis.RX,
                    cameraTiltLeftRight,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gpName,
                    Component.Identifier.Axis.RY,
                    cameraTiltUpDown,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gpName,
                    Component.Identifier.Axis.Z,
                    cameraMoveRoll,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        catch (Exception e){
            System.out.println("No Controller Detected");
        }

        // attach the action objects to keyboard and gamepad components
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.ESCAPE,
                quitGameAction,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.C,
                incrementCounterAction,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.V,
                cameraChangeView,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }

    @Override
    protected void update(Engine engine) {
        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();
        elapsTime += engine.getElapsedTimeMillis();
        elapsTimeSec = Math.round(elapsTime/1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);
        counterStr = Integer.toString(counter);
        im.update(elapsTime);
        dispStr = hudContent("  Visited Planets = " + counterStr);
        rs.setHUD(dispStr, 13, 13);
        checkDistance();
        muberGame();
    }

    // ======== This will update the HUD when the player gets too far from the dolphin. ===============
    private String hudContent(String display){
        String content = display;
        if(stop && getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'c'){
            content = content + "       Too far from dolphin, Press B(Controller) or SpaceBar(Keyboard)." ;
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
                -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 2.0f, -1.0f, // Top Left
                1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 2.0f, -1.0f, // Top Right
                -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, //UF
                1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f //UR

        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
        };

        float[] normals = new float[]{
                0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };

        int[] indices = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};

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

    //============ My Muber Game Logic=========================================================
    private void muberGame(){
        if(getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'c'){
            float distanceLimit = 0.3f;
            float distanceX, distanceZ, distanceY;
            Vector3 cameraPosition = getEngine().getSceneManager().getCamera("MainCamera").getPo();

            for(int i = 0; i < maxPlanets; i++){
                SceneNode muberNode = getEngine().getSceneManager().getSceneNode("diamondNode" + i);
                distanceX = Math.abs(muberNode.getLocalPosition().x() - cameraPosition.x());
                distanceY = Math.abs(muberNode.getLocalPosition().x() - cameraPosition.x());
                distanceZ = Math.abs(muberNode.getLocalPosition().z() - cameraPosition.z());

                if (getEngine().getSceneManager().getSceneNode("babycarry").getAttachedObjectCount() <= 0){
                    if (distanceX > distanceLimit && distanceZ > distanceLimit && distanceY > distanceLimit){
                        System.out.println("TRIES");
                        getEngine().getSceneManager().getSceneNode("babycarry").attachChild(muberNode);
                    }
                }
            }

        }
    }
    //==========================================================================================


    public void incrementCounter() {
        counter++;
    }

    public float getElapsTime(){
        return elapsTimeSec;
    }

}
