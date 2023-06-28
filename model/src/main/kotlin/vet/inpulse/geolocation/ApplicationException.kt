package vet.inpulse.geolocation

/**
 * Use this to wrap an application error.
 */
class ApplicationException(val error: Error, override val cause: Throwable? = null): Exception(cause)