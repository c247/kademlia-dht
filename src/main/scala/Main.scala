import scala.collection.mutable

case class ID(value: String)

case class Node(id: ID, address: String)

case class DHTValue(data: String)


class KademliaDHT {
  private val k = 20 // The "k" parameter in Kademlia
  private val nodes = mutable.ListBuffer[Node]()
  private val values = mutable.Map[ID, DHTValue]()

  def addNode(node: Node): Unit = {
    nodes += node
  }

  def storeValue(key: ID, value: DHTValue): Unit = {
    values += (key -> value)
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
}

object ID {
  // Calculate XOR distance between two IDs
  def distance(a: ID, b: ID): BigInt = {
    BigInt(a.value, 16) ^ BigInt(b.value, 16)
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val dht = new KademliaDHT()

    val node1 = Node(ID("0001"), "127.0.0.1:10001")
    val node2 = Node(ID("0010"), "127.0.0.1:10002")
    val node3 = Node(ID("0011"), "127.0.0.1:10003")

    dht.addNode(node1)
    dht.addNode(node2)
    dht.addNode(node3)

    val key = ID("1100")
    val value = DHTValue("Hello, Kademlia!")

    dht.storeValue(key, value)

    val closestNodes = dht.findClosestNodes(ID("1101"))
    println("Closest nodes to 1101:")
    closestNodes.foreach(println)

    val foundValue = dht.findValue(key)
    foundValue.foreach(v => println(s"Found value: $v"))
  }
}
