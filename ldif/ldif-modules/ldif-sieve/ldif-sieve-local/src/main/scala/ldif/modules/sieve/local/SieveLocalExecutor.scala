package ldif.modules.sieve.local

import ldif.module.Executor
import ldif.local.runtime._
import impl.NoEntitiesLeft
import org.apache.commons.io.FileUtils
import ldif.local.util.TemporaryFileCreator
import xml.{XML, Source}
import org.slf4j.LoggerFactory
import java.io.{FileInputStream, File}
import ldif.runtime.Quad
import ldif.util.Prefixes
import ldif.entity.{Node, Entity, EntityDescription}
import ldif.modules.sieve.fusion.{PassItOn, FusionFunction, TrustYourFriends, KeepFirst}
import ldif.modules.sieve.{SieveConfig, SieveTask}

/**
 * Executes Sieve on a local machine.
 * @author pablomendes - based on Silk and R2R executors.
 */
class SieveLocalExecutor(useFileInstanceCache: Boolean = false) extends Executor
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  //private val numThreads = 8
  //private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = SieveTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : SieveTask) : InputFormat =
  {
    implicit val prefixes = task.sieveConfig.sieveConfig.prefixes
    //log.info("Prefixes:"+prefixes.toString)

    // here we create entity descriptions from the task.sieveSpec
    //        val entityDescriptions = CreateEntityDescriptions(task.sieveSpec)
    val entityDescriptions = SieveConfig.createDummyEntityDescriptions(prefixes)

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : SieveTask) = new GraphFormat()

  /**
   * Executes a Sieve task.
   */
  override def execute(task : SieveTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("Executing Sieve Task %s".format(task.name))

    // for each entity reader (one per input file?)
    reader.foreach( in => {
      val nPatterns = in.entityDescription.patterns.length

      var entity : Entity = NoEntitiesLeft;
      while ( { entity = in.read(); entity != NoEntitiesLeft} ) {
        //log.info("Sieve Entity: %s".format(entity.resource.toString))
        //log.info("Patterns: "+entity.entityDescription.patterns.size)

        if (entity==null) {
          log.error("Is it normal that some entities will be intermittently null? %s".format(in.entityDescription))
        }

        if (entity!=null && entity!=NoEntitiesLeft) {
          for (patternId <- 0 until nPatterns) {
            val factums = entity.factums(patternId)
            val outputPropertyName = task.sieveSpec.outputPropertyNames(patternId)
            val fusionFunction = task.sieveSpec.fusionFunctions(patternId)
            //log.debug("Patern %s: FusionFunction used: %s".format(pattern, fusionFunction))

            if (factums.size==1) { //nothing to fuse //TODO filterign can also be
              val patternNodes = factums.head
              val propertyValue = patternNodes(0)
              val quad = new Quad(entity.resource, outputPropertyName, propertyValue, propertyValue.graph);
              writer.write(quad)
            } else {               // fuse multiple values into one value and write it out
              fusionFunction.fuse(factums).foreach( patternNodes => { // for each property
                if (patternNodes.nonEmpty) {
                  val propertyValue = patternNodes(0) //TODO deal with case where the pattern is a tree (more than one path)
                  val quad = new Quad(entity.resource, outputPropertyName, propertyValue, propertyValue.graph);
                  writer.write(quad)
                }
              })
            }

          }
        }
      }
    })

  }

}