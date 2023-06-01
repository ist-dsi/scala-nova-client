package pt.tecnico.dsi.openstack.nova.models

import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.{Configuration, ConfiguredDecoder, ConfiguredEncoder, renaming}
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, HCursor}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import squants.information.InformationConversions.*
import squants.information.{Gibibytes, Information, InformationUnit, Mebibytes}

object Flavor:
  private val baseRenames: Map[String, String] = Map(
    "ephemeral" -> "OS-FLV-EXT-DATA:ephemeral",
    "disabled" -> "OS-FLV-DISABLED:disabled",
    "isPublic" -> "os-flavor-access:is_public",
  ).withDefault(renaming.snakeCase)
  given Configuration = Configuration.default.withDefaults.withTransformMemberNames(baseRenames)

  case class Summary(
    id: String,
    name: String,
    description: Option[String] = None,
    links: List[Link] = List.empty,
  ) extends Identifiable derives ConfiguredDecoder
  
  object Create:
    given Encoder[Create] = (create: Create) => ConfiguredEncoder.derived[Create].mapJsonObject { obj =>
      // The information encoder in the package object encodes all informations to mebibytes. These two are in gibibytes.
      obj.add(baseRenames("disk"), create.disk.toGibibytes.toInt.asJson)
        .add(baseRenames("ephemeral"), create.ephemeral.toGibibytes.toInt.asJson)
    }.apply(create)
  case class Create(
    id: Option[String] = None,
    name: String,
    description: Option[String] = None,
    vcpus: Int,
    ram: Information,
    disk: Information,
    ephemeral: Information = 0.gibibytes,
    swap: Information = 0.mebibytes,
    rxtxFactor: Float = 1.0f,
    disabled: Option[Boolean] = None,
    isPublic: Boolean = true,
  ) derives ShowPretty
  
  object Update:
    given Encoder[Update] = (update: Update) => ConfiguredEncoder.derived[Update].mapJsonObject { obj =>
      // The information encoder in the package object encodes all informations to mebibytes. These two are in gibibytes.
      obj.add(baseRenames("disk"), update.disk.map(_.toGibibytes.toInt).asJson)
        .add(baseRenames("ephemeral"), update.ephemeral.map(_.toGibibytes.toInt).asJson)
    }.apply(update)
  sealed case class Update(
    name: Option[String] = None,
    description: Option[String] = None,
    vcpus: Option[Int] = None,
    ram: Option[Information] = None,
    disk: Option[Information] = None,
    ephemeral: Option[Information] = None,
    swap: Option[Information] = None,
    rxtxFactor: Option[Float] = None,
    disabled: Option[Boolean] = None,
    isPublic: Option[Boolean] = None,
  ) derives ShowPretty:
    lazy val needsUpdate: Boolean =
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(name, description, vcpus, ram, disk, ephemeral, swap, rxtxFactor, disabled, isPublic).exists(_.isDefined)
  
  given Encoder[Flavor] = (flavor: Flavor) => ConfiguredEncoder.derived[Flavor].mapJsonObject { obj =>
    // The information encoder in the package object encodes all informations to mebibytes. These two are in gibibytes.
    obj.add(baseRenames("disk"), flavor.disk.toGibibytes.toInt.asJson)
      .add(baseRenames("ephemeral"), flavor.ephemeral.toGibibytes.toInt.asJson)
  }.apply(flavor)
  
  def informationDecoderIn(unit: InformationUnit): Decoder[Information] = Decoder.decodeInt.map(unit(_))
  val inGibibytesDecoder: Decoder[Information] = informationDecoderIn(Gibibytes)
  val inMebibytesDecoder: Decoder[Information] = informationDecoderIn(Mebibytes)
  
  given Decoder[Flavor] = (cursor: HCursor) => for
    id <- cursor.get[String]("id")
    name <- cursor.get[String]("name")
    description <- cursor.get[Option[String]]("description")
    vcpus <- cursor.get[Int]("vcpus")
    ram <- cursor.get[Information]("ram")(inMebibytesDecoder)
    disk <- cursor.get[Information]("disk")(inGibibytesDecoder)
    ephemeral <- cursor.get[Information](baseRenames("ephemeral"))(inGibibytesDecoder)
    swap <- cursor.get[Information]("swap")(inMebibytesDecoder).orElse(Right(0.mebibytes))
    rxtxFactor <- cursor.get[Float]("rxtx_factor")
    disabled <- cursor.getOrElse[Boolean](baseRenames("disabled"))(false)
    isPublic <- cursor.getOrElse[Boolean](baseRenames("isPublic"))(true)
    extraSpecs <- cursor.getOrElse[Map[String, String]]("extra_specs")(Map.empty)
    links <- cursor.getOrElse[List[Link]]("links")(List.empty)
  yield Flavor(id, name, description, vcpus, ram, disk, ephemeral, swap, rxtxFactor, disabled, isPublic, extraSpecs, links)
sealed case class Flavor(
  id: String,
  name: String,
  description: Option[String] = None,
  
  vcpus: Int,
  ram: Information,
  disk: Information,
  ephemeral: Information = 0.gibibytes,
  swap: Information = 0.mebibytes,
  rxtxFactor: Float = 1.0f,
  disabled: Boolean = false,
  isPublic: Boolean = true,
  extraSpecs: Map[String, String] = Map.empty,
  
  links: List[Link] = List.empty,
) extends Identifiable derives ShowPretty
