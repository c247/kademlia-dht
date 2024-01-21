import scala.collection.mutable

case class ID(value: String)

case class Node(id: ID, address: String)

case class DHTValue(data: String)

class KademliaDHT {
  private val k = 20
  private val replicationFactor = 3
  private val nodes = mutable.ListBuffer[Node]()
  private val values = mutable.Map[ID, DHTValue]()
  private val replicationNodes = mutable.Map[ID, List[Node]]()

  def addNode(node: Node): Unit = {
    nodes += node
    replicateValues(node)
  }

  def storeValue(key: ID, value: DHTValue): Unit = {
    values += (key -> value)
    replicationNodes.getOrElseUpdate(key, List()).foreach { node =>
      values += (key -> value)
    }
  }

  def findClosestNodes(target: ID): List[Node] = {
    nodes
      .sortBy(node => ID.distance(node.id, target))
      .take(k)
      .toList
  }

  def findValue(key: ID): Option[DHTValue] = {
    values.get(key)
  }

  def getNodeCount: Int = nodes.length

  def getValueCount: Int = values.size

  def iterativeNodeLookup(target: ID): List[Node] = {
    var closestNodes = findClosestNodes(target)
    var alreadyQueried = Set[ID]()

    while (closestNodes.nonEmpty && alreadyQueried.size < k) {
      val currentNode = closestNodes.head
      closestNodes = closestNodes.tail :+ findClosestNodes(currentNode.id)

      alreadyQueried += currentNode.id
    }

    closestNodes.flatten.distinct.take(k).toList
  }

  private def replicateValues(node: Node): Unit = {
    replicationNodes.foreach { case (key, nodesForKey) =>
      if (nodesForKey.length < replicationFactor && !nodesForKey.contains(node)) {
        replicationNodes += (key -> (node :: nodesForKey))
      }
    }
  }

  def handleNetworkPartition(node: Node): Unit = {
    val remainingNodes = nodes.filterNot(_ == node)
    nodes.clear()
    nodes ++= remainingNodes
  }

  def printDHTState(): Unit = {
    println("DHT State:")
    println(s"Total Nodes: ${getNodeCount}")
    println(s"Total Values: ${getValueCount}")

    println("\nNodes:")
    nodes.foreach(println)

    println("\nValues:")
    values.foreach { case (key, value) =>
      println(s"$key -> $value")
    }
  }
}

object ID {
  def distance(a: ID, b: ID): BigInt = {
    BigInt(a.value, 16) ^ BigInt(b.value, 16)
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val dht = new KademliaDHT()

    // Adding nodes
    val nodesToAdd = List(
      Node(ID("0001"), "127.0.0.1:10001"),
      Node(ID("0010"), "127.0.0.1:10002"),
      Node(ID("0011"), "127.0.0.1:10003"),
      Node(ID("0100"), "127.0.0.1:10004"),
      Node(ID("0101"), "127.0.0.1:10005"),
      Node(ID("0110"), "127.0.0.1:10006"),
      Node(ID("0111"), "127.0.0.1:10007"),
      Node(ID("1000"), "127.0.0.1:10008"),
      Node(ID("1001"), "127.0.0.1:10009"),
      Node(ID("1010"), "127.0.0.1:10010"),
      Node(ID("1011"), "127.0.0.1:10011"),
      Node(ID("1100"), "127.0.0.1:10012")
    )

    nodesToAdd.foreach(dht.addNode)

    // Storing values
    val keysAndValues = List(
      ID("1100") -> DHTValue("Hello, Kademlia!"),
      ID("1010") -> DHTValue("Kademlia DHT example"),
      ID("0101") -> DHTValue("Scala is awesome"),
      ID("1111") -> DHTValue("More data for testing"),
      ID("1001") -> DHTValue("Iterative Lookup Test")
    )

    keysAndValues.foreach { case (key, value) =>
      dht.storeValue(key, value)
    }

    // Iterative Node Lookup
    val targetKey = ID("1001")
    val iterativeLookupResult = dht.iterativeNodeLookup(targetKey)
    println(s"Iterative Node Lookup for $targetKey:")
    iterativeLookupResult.foreach(println)

    // Finding values
    val foundValue = dht.findValue(keysAndValues.head._1)
    foundValue.foreach(v => println(s"Found value: $v"))

    // Network Partition Handling
    val partitionedNode = nodesToAdd.head
    dht.handleNetworkPartition(partitionedNode)
    println(s"\nNetwork partitioned: $partitionedNode")

    // Print DHT State
    dht.printDHTState()
  }
}
