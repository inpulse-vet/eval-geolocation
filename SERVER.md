# eval-geolocation
INpulse internal project to help evaluate candidates

## The System to be developed

* A web server made in Kotlin on top of Ktor, used to list the nearby restaurants within a given geolocation.
* This is a set of requirements. It's the developer's job to decide how this system should be implemented.

All data received or sent within the HTTP request body must use JSON encoding.

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

Attention: This endpoint requires authentication!
Authentication will use the HTTP Basic Authentication Scheme

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
  
### Non-functional requirements
* Code clarity and quality
* Less than one second response even when requesting a large (>100) number of items. The fastest, the better.
* Support tens of millions of items in its database

### Bonus points if done:
* Deploy using OCI compatible image
* Graceful stop: After receiving a shutdown command, backend halts new connections, serves in-flight requests and only then exits.
* Readiness endpoint
* Healthcheck endpoint
* Metrics collection endpoint
* Tracing. OpenTelemetry compatibility
* Unit or integration tests.

### Others
Candidate will be provided with a minimally configured gradle multimodule project along with some client-side
code to be used for consuming the REST API exposed by the system's backend.

The client-side code will require some adjustment in order do function properly.
Some integration tests that use the client-side API are expected to be implemented by the candidate.
These tests can be used to evaluate the system's backend code correctness. An example test implementation is also provided.

Candidate can use any available Kotlin or Java library.
One can assume this system is only targeting the JVM 17 platform.

We (INpulse) expect that this task to be completed in, at most, 8 working hours, not counting breaks,
not including the bonus points presented.

### Questions

Candidate can ask for clarifications in case he/she didn't fully understand at any time, but some autonomy is expected.

### Evaluation

Candidates will be evaluated through a code review process along with questions in respect to the decisions he/she has made.
We expect the candidate to have a solid understanding of every choice he has made and what impact it has on code
quality, legibility, maintainability and performance. We also expect the candidate to present thoughts about other options
and weight pros and cons when compared to the chosen one.

Knowledge about data structures and their best use cases is a big plus.