package example.bridge

// Environment code for project grid

import jason.asSyntax.Structure
import jason.environment.Environment
import jason.future.EnvironmentModel
import jason.future.MatrixCapable
import jason.runtime.RuntimeServicesFactory
import java.util.logging.Logger
import kotlin.concurrent.thread

class BridgeJasonEnv : Environment(1), MatrixCapable<BridgeState, BridgeAction> { // single thread environment
    private val model   = BridgeEnvModel(
        BridgeState(3, 9, 17, 10, 0), // initial state
    )
    private val log :Logger  = Logger.getLogger("bridge-env")

    override fun init(args: Array<String>?) {
        var gui = true
        if (!args.isNullOrEmpty()) {
            for (a in args) {
                if (a.startsWith("no_gui")) {
                    gui = false
                }
            }
        }
        if (gui) {
            val view = BridgeGridEnvView(model, this)
            view.resetGUI()
        }
        thread(start = true) {
                // wait for some agent to be created
                while (RuntimeServicesFactory.get().agentsName.isEmpty())
                    Thread.sleep(200)

                updatePercept()
        }
    }

    override fun getModel(): EnvironmentModel<BridgeState, BridgeAction> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        val ag = model.agNameToNb(agName)
        val prePos = model.getAgPos(ag)
        model.execute( model.structureToAction(agName, action) )
        log.info("ag $ag executing: $action. from $prePos to ${model.getAgPos(ag)}")
        Thread.sleep(300)
        //updateAgPercept(agName)
        updatePercept()
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    fun updatePercept() {
        for (agName in RuntimeServicesFactory.get().agentsName) {
            updateAgPercept(agName)
            informAgsEnvironmentChanged()
        }
    }

    private fun updateAgPercept(agName: String) {
        clearPercepts(agName)
        addPercept(agName, *(model.agPerception(agName).toTypedArray()))
    }
}
