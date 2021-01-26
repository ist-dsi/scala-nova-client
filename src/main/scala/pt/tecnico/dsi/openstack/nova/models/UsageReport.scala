package pt.tecnico.dsi.openstack.nova.models

import java.time.LocalDateTime
import cats.derived
import cats.derived.ShowPretty
import io.circe.{Decoder, HCursor}
import squants.information.Information
import io.chrisdavenport.cats.time.localdatetimeInstances
import squants.information.InformationConversions._

object ServerUsage {
  implicit val show: ShowPretty[ServerUsage] = derived.semiauto.showPretty
  // Custom decoder mainly because local_gb is in GiB and memory_mb is in MiB
  implicit val decoder: Decoder[ServerUsage] = (cursor: HCursor) => for {
    name <- cursor.get[String]("name")
    instanceId <- cursor.get[String]("instance_id")
    flavor <- cursor.get[String]("flavor")
    hours <- cursor.get[Double]("hours")
    disk <- cursor.get[Information]("local_gb")(Decoder.decodeDouble.map(_.gibibytes))
    memory <- cursor.get[Information]("memory_mb")(Decoder.decodeDouble.map(_.mebibytes))
    vcpus <- cursor.get[Int]("vcpus")
    state <- cursor.get[String]("state")
    uptime <- cursor.get[Int]("uptime")
    startedAt <- cursor.get[LocalDateTime]("started_at")
    endedAt <- cursor.getOrElse[Option[LocalDateTime]]("ended_at")(Option.empty)
  } yield ServerUsage(name, instanceId, flavor, hours, disk, memory, vcpus, state, uptime, startedAt, endedAt)
}
case class ServerUsage(
  name: String,
  instanceId: String,
  flavor: String,
  hours: Double,
  disk: Information,
  memory: Information,
  vcpus: Double,
  state: String,
  uptime: Int,
  startedAt: LocalDateTime,
  endedAt: Option[LocalDateTime] = None,
)

object UsageReport {
  implicit val show: ShowPretty[UsageReport] = derived.semiauto.showPretty
  // Custom decoder mainly because total_local_gb_usage is in GiB and total_memory_mb_usage is in MiB
  implicit val decoder: Decoder[UsageReport] = (cursor: HCursor) => for {
    start <- cursor.get[LocalDateTime]("start")
    stop <- cursor.get[LocalDateTime]("stop")
    projectId <- cursor.get[String]("tenant_id")
    totalHours <- cursor.get[Double]("total_hours")
    totalDiskUsage <- cursor.get[Information]("total_local_gb_usage")(Decoder.decodeDouble.map(_.gibibytes))
    totalMemoryUsage <- cursor.get[Information]("total_memory_mb_usage")(Decoder.decodeDouble.map(_.mebibytes))
    totalVCPUsUsage <- cursor.get[Double]("total_vcpus_usage")
    serverUsages <- cursor.getOrElse[List[ServerUsage]]("server_usages")(List.empty)
  } yield UsageReport(start, stop, projectId, totalHours, totalDiskUsage, totalMemoryUsage, totalVCPUsUsage, serverUsages)
}
case class UsageReport(
  start: LocalDateTime,
  stop: LocalDateTime,
  projectId: String,
  totalHours: Double,
  totalDiskUsage: Information,
  totalMemoryUsage: Information,
  totalVCPUsUsage: Double,
  serverUsages: List[ServerUsage] = List.empty,
)
