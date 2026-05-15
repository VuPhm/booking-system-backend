package edu.uaf.booking.repository;

import edu.uaf.booking.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProviderRepository extends JpaRepository<Provider, Long> {
}
