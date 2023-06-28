# eval-geolocation
INpulse internal project to help evaluate candidates

## The System to be developed

* A web server made in Kotlin on top of Ktor, used to list the nearby restaurants within a given geo-location.
* This is not a technical specification but a set of requirements. The developer's job is to decide how exactly this should be executed.

### Endpoints

#### Get nearby restaurants:
`GET /restaurants?lat={lat}&long={long}&n={n}[&distance={distance}]`
  * inputs: latitude, longitude, number of results
  * optional inputs: distance (km) - maximum distance from coordinate (lat, long)

Responses: 
* `200 OK` - `List<Restaurant>` found nearby.
* `400 Bad Request` if any of the inputs is malformed. 

#### Get restaurant details:
`GET /restaurant/{restaurantId}/`

With the restaurant's `id`, get more info about the place.

Responses:
* `200 OK` - `RestaurantDetails` of the restaurant referenced by its `id`.
* `400 Bad Request`  if `id` is malformed.
* `404 Not Found` if `id` does not exist.

#### Add new restaurant 

`POST /restaurants`

Receives a `RestaurantDetail` in request body and inserts it into the database.

Responses:
* `201 Created` if `Restaurant` has been successfully inserted into the database.
* `400 Bad Request` when `RestaurantDetail` data is malformed
* `401 Not Authorized` when requesting client doesn't have valid credentials

### Models

* `Location`:
  * `lat: Latitude`
  * `long: Longitude`

* `Restaurant`:
  * `id: UUID`
  * `name: String`
  * `location: Location`

* `RestaurantDetail`
  * `id: UUID`
  * `name: String`
  * `location: Location`
  * `streetAddress: String`
  * `phone: String?`
  * `website: Url?`
  * `openHours: OpenHours?`

* `OpenHours`
  * `openingTime: Time`
  * `closingTime: Time`

* `Time` - `ISO-8601` formatted time (24 hour clock). Example: `13:30:00`
* `Latitude` - decimal format. Example: `43.12312`
* `Longitude` - decimal format. Example: `122.12312`
  






