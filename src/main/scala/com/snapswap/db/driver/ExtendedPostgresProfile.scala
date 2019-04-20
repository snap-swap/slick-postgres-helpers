package com.snapswap.db.driver

import java.sql.Timestamp
import java.time.{ZoneOffset, ZonedDateTime}

import com.github.tminglei.slickpg._
import slick.basic.Capability
import spray.json._


trait ExtendedPostgresProfile
  extends ExPostgresProfile
    with PgArraySupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSprayJsonSupport
    with PgSearchSupport
    with PgNetSupport
    with PgLTreeSupport {

  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api: ApiWithExtendedTypeSupport = ApiWithExtendedTypeSupport

  object ApiWithExtendedTypeSupport extends ApiWithExtendedTypeSupport

  trait ApiWithExtendedTypeSupport extends API
    with ArrayImplicits
    with JsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {

    implicit val strListTypeMapper: BaseColumnType[List[String]] = new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val playJsonArrayTypeMapper: BaseColumnType[List[JsValue]] =
      new AdvancedArrayJdbcType[JsValue](pgjson,
        s => utils.SimpleArrayUtils.fromString[JsValue](_.parseJson)(s).orNull,
        v => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)

    implicit val JavaZonedDateTimeMapper: BaseColumnType[ZonedDateTime] = MappedColumnType.base[ZonedDateTime, Timestamp](
      z => Timestamp.valueOf(z.toInstant.atZone(ZoneOffset.UTC).toLocalDateTime),
      t => t.toLocalDateTime.atZone(ZoneOffset.UTC)
    )
  }

}

object ExtendedPostgresProfile extends ExtendedPostgresProfile
