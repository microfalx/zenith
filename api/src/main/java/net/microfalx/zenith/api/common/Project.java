package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;

/**
 * A project running a Selenium {@link Session}.
 */
@Getter
@ToString
public class Project extends NamedIdentityAware<String> {

    private boolean active;

    public Builder create(String id) {
        return new Builder(id);
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private boolean active;

        public Builder(String id) {
            super(id);
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Project();
        }

        @Override
        public Project build() {
            Project project = (Project) super.build();
            project.active = active;
            return project;
        }
    }
}
