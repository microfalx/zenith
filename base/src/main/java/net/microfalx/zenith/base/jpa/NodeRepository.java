package net.microfalx.zenith.base.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRepository extends NaturalJpaRepository<Node, Integer>, JpaSpecificationExecutor<Node> {
}
