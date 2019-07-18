package avro.mirrormaker;

import com.hortonworks.registries.schemaregistry.serdes.avro.kafka.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class ConfluentToHortonSerializer implements AvroSerializer {
    private KafkaAvroDeserializer confluentDeserializer;
    private KafkaAvroSerializer hortonSerializer;

    public ConfluentToHortonSerializer() {
        confluentDeserializer = new KafkaAvroDeserializer();
        hortonSerializer = new KafkaAvroSerializer();
    }

    public void configure(String confluentUrl, String hortonUrl) {
        Map<String, Object> confluentConfig = new HashMap<>();
        Map<String, Object> hortonConfig = new HashMap<>();

        confluentConfig.put("schema.registry.url", confluentUrl);
        hortonConfig.put("schema.registry.url", hortonUrl);

        confluentDeserializer.configure(confluentConfig, false);
        hortonSerializer.configure(hortonConfig, false);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Map<String, Object> confluentConfig = new HashMap<>(configs);
        Map<String, Object> hortonConfig = new HashMap<>(configs);

        confluentConfig.put("schema.registry.url", configs.get(Common.CONFLUENT_SCHEMA_REGISTRY_URL_CONFIG));
        hortonConfig.put("schema.registry.url", configs.get(Common.HORTON_SCHEMA_REGISTRY_URL_CONFIG));

        confluentDeserializer.configure(confluentConfig, false);
        hortonSerializer.configure(hortonConfig, false);
    }

    @Override
    public byte[] serialize(String topic, byte[] data) {
        return hortonSerializer.serialize(topic, confluentDeserializer.deserialize(topic, data));
    }

    @Override
    public void close() {
        confluentDeserializer.close();
        hortonSerializer.close();
    }
}