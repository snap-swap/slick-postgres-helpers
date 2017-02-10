package com.snapswap.db.helpers.runner

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.snapswap.db.errors.{EntityNotFound, InternalDataError}
import org.scalatest.{AsyncWordSpec, Matchers, OneInstancePerTest}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

class DbRunHelperSpec
  extends AsyncWordSpec
    with Matchers
    with OneInstancePerTest {

  import runSafe._

  val epg: EmbeddedPostgres = EmbeddedPostgres.start
  val db: Database = Database.forDataSource(epg.getPostgresDatabase)
  val tbl: TableQuery[TestTable] = TableQuery[TestTable]

  "SlickRunWrapper" should {
    "replace known errors by appropriate DataError" in {
      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe(sql"""select "IntColumn" from "TestTable" """.as[Int].head)
      } yield result

      action.failed.map { r =>
        r shouldBe a[EntityNotFound]
      }
    }
    "replace unknown errors by InternalDataError with original error description" in {
      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe(DBIO.seq(sql"""select IntColumn from NotExistingTable""".as[Int]).map(_ => ()))
      } yield result

      action.failed map { result =>
        result shouldBe a[InternalDataError]
        result.getMessage should startWith("ERROR: relation \"notexistingtable\" does not exist\n  Position: 23")
      }
    }
    "when success - return Future result" in {
      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe((tbl += 1).andThen(tbl.map(_.IntColumn).result))
      } yield result

      action.map { result =>
        result should equal(Seq(1))
      }
    }
  }

  class TestTable(tag: Tag) extends Table[Int](tag, "TestTable") {
    def IntColumn = column[Int]("IntColumn")

    def * = IntColumn
  }

}