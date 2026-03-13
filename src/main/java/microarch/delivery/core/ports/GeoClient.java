package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.kernel.Address;
import microarch.delivery.core.domain.model.kernel.Location;

public interface GeoClient {
    Location getLocation(Address address);
}
