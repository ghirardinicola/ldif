package ldif.modules.sieve.fusion

/*
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import functions.PassItOn
import java.io.{FileInputStream, InputStream, File}
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import ldif.entity.EntityDescription
import org.slf4j.LoggerFactory
import ldif.util.Prefixes

/**
 *
 * sieveSpecs: contains one spec per <Class> tag in the configuration.
 *             Each spec describes how to process one entity description (restricted to Class, with a pattern for each Property)
 *             Each spec holds a list of corresponding FusionFuctions for each property
 *
 * @author pablomendes
 */
class FusionConfig(val prefixes: Prefixes,
                   val sieveSpecs: Traversable[FusionSpecification],
                   val entityDescriptions: Seq[EntityDescription]) {

  def merge(c: FusionConfig) : FusionConfig = { //TODO implement
    throw new NotImplementedException
    //this
  }

}

object FusionConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)
  private val schemaLocation = "de/fuberlin/wiwiss/sieve/Sieve.xsd"

  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty) = {
    val specs = (node \ "Class").map(FusionSpecification.fromXML)
    val entityDescriptions = (node \ "Class").map(FusionEntityDescription.fromXML(_)(prefixes))
    new FusionConfig(prefixes, specs, entityDescriptions)
  }
// Example from Silk:
//object LinkingConfig {
//  private val schemaLocation = "de/fuberlin/wiwiss/silk/LinkSpecificationLanguage.xsd"
//
//  def empty = LinkingConfig(Prefixes.empty, RuntimeConfig(), Nil, Nil, Nil)
//
//  def load = {
//    new ValidatingXMLReader(fromXML, schemaLocation)
//  }
//
//  def fromXML(node: Node) = {
//    implicit val prefixes = Prefixes.fromXML(node \ "Prefixes" head)
//    val sources = (node \ "DataSources" \ "DataSource").map(Source.fromXML)
//    val blocking = (node \ "Blocking").headOption match {
//      case Some(blockingNode) => Blocking.fromXML(blockingNode)
//      case None => Blocking()
//    }
//    val linkSpecifications = (node \ "Interlinks" \ "Interlink").map(p => LinkSpecification.fromXML(p))
//
//    implicit val globalThreshold = None
//    val outputs = (node \ "Outputs" \ "Output").map(Output.fromXML)
//
//    LinkingConfig(prefixes, RuntimeConfig(blocking), sources, linkSpecifications, outputs)
//  }
//}



  def createDummyEntityDescriptions(prefixes: Prefixes) : List[EntityDescription] = {
    // read from jar
    //val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music_EntityDescription.xml")
    // read from file
    //val stream = new FileInputStream("/home/pablo/workspace/ldif/ldif/ldif-modules/ldif-sieve/ldif-sieve-local/src/test/resources/ldif/modules/sieve/local/Music_EntityDescription.xml");

    //if (stream!=null) {
//      val testXml = XML.load(stream);
      val testXml = FusionEntityDescription.createLwdm2012EntityDescription

      val e = EntityDescription.fromXML(testXml)(prefixes)
      log.debug("[FUSION] "+e.toString);
      List(e)
//    } else {
//      log.error("EntityDescription returned null!");
//      List() //empty?
//    }
  }

  def empty : EmptyFusionConfig = {
    new EmptyFusionConfig
  }
}

/*
 This class should never be actually used for fusion. It simply signals that no config exists, and the framework should repeat the input.
 */
class EmptyFusionConfig extends FusionConfig(Prefixes.stdPrefixes,
                                List(new FusionSpecification("Default",
                                                             IndexedSeq(new PassItOn),
                                                             IndexedSeq("DEFAULT")
                                                             )),
                                List(EntityDescription.empty)

) {

}


  /*
<http://dbpedia.org/ontology/musicalArtist>
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
<http://www.w3.org/2000/01/rdf-schema#label>
<http://www.w3.org/2002/07/owl#sameAs>
<http://xmlns.com/foaf/0.1/made>
<http://xmlns.com/foaf/0.1/member>

provenance triples:
<http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportType>
<http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation>
<http://www4.wiwiss.fu-berlin.de/ldif/importId>
<http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate>

   */