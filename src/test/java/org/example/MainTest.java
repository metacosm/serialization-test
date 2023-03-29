package org.example;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    void foo() throws JsonProcessingException {
        final var original = new TestCR();
        final var foo = new FooChild();
        foo.setName("alice");
        final var bar = new BarChild();
        bar.setFile("bob");
        original.getSpec().setChildren(List.of(foo, bar));
        final var mapper = new ObjectMapper();
        mapper.registerModules(new JavaTimeModule());
        final var asString = mapper.writeValueAsString(original);
        final var json = Serialization.asJson(original);
        System.out.println("json = " + json);
        System.out.println("asString = " + asString);
        assertThat(json).isEqualTo(asString);
        assertThat(json).contains(List.of("foo", "bar", "alice", "bob", "name", "file"));
    }

    @Group("example.com")
    @Version("v1alpha1")
    static class TestCR extends CustomResource<TestCR.TestCRSpec, Void> implements Namespaced {

        @JsonDeserialize(using = JsonDeserializer.None.class)
        static class TestCRSpec implements KubernetesResource {

            private List<Child> children = new ArrayList<>();

            public List<Child> getChildren() {
                return children;
            }

            public void setChildren(List<Child> children) {
                this.children = children;
            }
        }

        @Override
        protected TestCRSpec initSpec() {
            return new TestCRSpec();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({ //
            @JsonSubTypes.Type(FooChild.class), //
            @JsonSubTypes.Type(BarChild.class), //
    })
    @JsonDeserialize(using = JsonDeserializer.None.class)
    interface Child extends KubernetesResource {
    }

    @JsonTypeName("foo")
    static class FooChild implements Child {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonTypeName("bar")
    static class BarChild implements Child {
        private String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }

}