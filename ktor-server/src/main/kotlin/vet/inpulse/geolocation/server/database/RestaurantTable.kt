package vet.inpulse.geolocation.server.database

import org.jetbrains.exposed.sql.*
import org.postgis.PGgeometry
import org.postgis.Point

object RestaurantTable : Table("restaurants") {
    val id = uuid("id")
    val name = varchar("name", 64)

    val location = point("location")
    val streetAddress = varchar("street_address", 128)

    val phone = varchar("phone", 15).nullable()
    val website = varchar("website", 128).nullable()

    val openingTime = varchar("opening_time", 256)
    val closingTime = varchar("closing_time", 256)

    override val primaryKey = PrimaryKey(id)
}

fun Table.point(name: String): Column<Point> = registerColumn(name, PointColumnType())

class PointColumnType(private val srid: Int = 4326) : ColumnType() {

    override fun sqlType() = "GEOMETRY(Point, $srid)"

    override fun valueFromDB(value: Any): Any = if (value is PGgeometry) value.geometry else value

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Point -> {
            if (value.srid == Point.UNKNOWN_SRID) value.srid = srid
            PGgeometry(value)
        }
        else -> value
    }
}

fun Column<Point>.distance(point2: Point, maxDistanceKm: Double): Op<Boolean> =
      DistanceOp(this, point2, maxDistanceKm)

class DistanceOp(
      private val point1: Column<Point>,
      private val point2: Point,
      private val maxDistanceKm: Double
) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("ST_Distance(ST_SetSRID(ST_MakePoint(")
        append(point2.x.toString())
        append(",")
        append(point2.y.toString())
        append("), 4326)::geography, ")
        append(point1.name)
        append("::geography) <= ")
        append(maxDistanceKm.toString())
    }
}