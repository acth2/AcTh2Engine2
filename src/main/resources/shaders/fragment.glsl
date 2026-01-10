#version 330

in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec2 outTexCoord;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PointLight
{
    vec3 color;
    vec3 position;
    float intensity;
    Attenuation att;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform Material material;
uniform vec3 ambientLight;
uniform float specularPower;
uniform PointLight pointLight;
uniform DirectionalLight directionalLight;
uniform mat4 viewMatrix;

vec4 calcLightColor(vec3 light_color, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specColor = vec4(0, 0, 0, 0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = material.diffuse * vec4(light_color, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_pos = -viewMatrix[3].xyz * mat3(viewMatrix);
    vec3 view_direction = normalize(camera_pos - position);
    vec3 halfway_direction = normalize(to_light_dir + view_direction);
    float spec_angle = max(dot(normal, halfway_direction), 0.0);
    float specularFactor = pow(spec_angle, specularPower);

    if (material.reflectance > 0) {
        specColor = material.specular * vec4(light_color, 1.0) * light_intensity * specularFactor * material.reflectance;
    }

    return (diffuseColor + specColor);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir = normalize(light_direction);

    // Attenuation
    float distance = length(light_direction);
    float attenuation = 1.0 / (light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance);

    return calcLightColor(light.color, light.intensity * attenuation, position, to_light_dir, normal);
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
    // The direction to the light is the opposite of the light's direction vector
    vec3 to_light_dir = -light.direction;
    return calcLightColor(light.color, light.intensity, position, normalize(to_light_dir), normal);
}

void main()
{
    vec4 baseColor;
    if ( material.hasTexture == 1 )
    {
        baseColor = texture(texture_sampler, outTexCoord);
    }
    else
    {
        baseColor = material.ambient;
    }

    vec3 normal = normalize(mvVertexNormal);
    vec4 totalLight = vec4(ambientLight, 1.0) * material.ambient;

    // Use point light
    totalLight += calcPointLight(pointLight, mvVertexPos, normal);

    // Uncomment to use directional light
    // totalLight += calcDirectionalLight(directionalLight, mvVertexPos, normal);

    fragColor = baseColor * totalLight;
}
