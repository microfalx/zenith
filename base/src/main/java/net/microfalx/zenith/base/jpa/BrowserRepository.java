package net.microfalx.zenith.base.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BrowserRepository extends NaturalJpaRepository<Browser, Integer>, JpaSpecificationExecutor<Browser> {
}
