#version 330

in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec2 outTexCoord;

out vec4 fragColor;

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

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

struct SpotLight {
    PointLight pl;
    vec3 coneDirection;
    float cutOff;
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
    vec4 colour;
    int hasTexture;
    float reflectance;
    int unlit;
};

uniform sampler2D texture_sampler;
uniform Material material;
uniform vec3 ambientLight;
uniform float specularPower;
uniform int pointLightCount;
uniform int spotLightCount;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;

vec4 calcLightColor(vec3 light_color, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specColor = vec4(0, 0, 0, 0);

    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = material.diffuse * vec4(light_color, 1.0) * light_intensity * diffuseFactor;

    vec3 view_direction = normalize(-position);
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

    float distance = length(light_direction);
    float attenuation = 1.0 / (light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance);

    return calcLightColor(light.color, light.intensity * attenuation, position, to_light_dir, normal);
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir = normalize(light_direction);
    float spot_factor = dot(to_light_dir, -light.coneDirection);

    vec4 light_color = vec4(0,0,0,0);
    if (spot_factor > light.cutOff) {
        light_color = calcPointLight(light.pl, position, normal);
    }
    return light_color;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
    vec3 to_light_dir = -light.direction;
    return calcLightColor(light.color, light.intensity, position, normalize(to_light_dir), normal);
}

void main()
{
    if (material.unlit == 1) {
        if (material.hasTexture == 1) {
            vec4 texel = texture(texture_sampler, outTexCoord);
            fragColor = vec4(material.colour.rgb, material.colour.a * texel.r);
        } else {
            fragColor = material.colour;
        }
        return;
    }

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

    for (int i = 0; i < pointLightCount; i++) {
        totalLight += calcPointLight(pointLights[i], mvVertexPos, normal);
    }

    for (int i = 0; i < spotLightCount; i++) {
        totalLight += calcSpotLight(spotLights[i], mvVertexPos, normal);
    }

    totalLight += calcDirectionalLight(directionalLight, mvVertexPos, normal);

    fragColor = baseColor * totalLight;
}
