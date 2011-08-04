package de.fuberlin.wiwiss.ldif.mapreduce

import ldif.entity.NodeWritable
import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, ArrayWritable, Writable}
import java.lang.Byte

case class ValuePathWritable (var pathID : IntWritable, var pathType: PathType, var values : ArrayWritable) extends Writable {

  def this() {this(new IntWritable(), EntityPathType, new ArrayWritable(classOf[ArrayWritable]))}

  def write(output : DataOutput) {
    pathID.write(output)
    output.writeByte(pathType.bytePathType)
    values.write(output)
  }

  def readFields(input : DataInput) {
    pathID.readFields(input)
    pathType = PathTypeMap(input.readByte)
    values.readFields(input)
  }

  override def toString = {
    val builder = new StringBuilder
    val pt = pathType match {
      case EntityPathType => "EntityPath"
      case JoinPathType => "JoinPath"
    }
    builder.append(pt).append("(").append(pathID).append(", ").append(values.toString).append(")")
    builder.toString
  }
}

sealed trait PathType {val bytePathType: Int}

case object EntityPathType extends PathType {val bytePathType = 0}

case object JoinPathType extends PathType {val bytePathType = 1}

case object PathTypeMap {
  val map = Map(0 -> EntityPathType, 1 -> JoinPathType)
  def apply(index: Int) = map(index)
}