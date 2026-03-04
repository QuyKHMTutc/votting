import org.springframework.data.redis.serializer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
public class TestSerializer {
    public static void main(String[] args) {
        System.out.println(GenericJackson2JsonRedisSerializer.class.getName());
        try {
            System.out.println(Class.forName("org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializerBuilder"));
        } catch (Exception e) {}
    }
}
