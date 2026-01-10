package fr.acth2.engine.utils.loader;

import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.utils.models.Face;
import fr.acth2.engine.utils.models.IdxGroup;
import fr.acth2.engine.utils.models.VertexKey;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Loader {
    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(Loader.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    public static List<String> readAllLines(String fileName) {
        try (InputStream in = Loader.class.getResourceAsStream(fileName)) {

            if (in == null) {
                throw new RuntimeException("Resource not found: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {

                List<String> lines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                return lines;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + fileName, e);
        }
    }

    public static Mesh loadMesh(String fileName) throws Exception {
        List<String> lines = readAllLines(fileName);

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Face> faces = new ArrayList<>();

        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    Vector3f vec3f = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vec3f);
                    break;
                case "vt":
                    Vector2f vec2f = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vec2f);
                    break;
                case "vn":
                    // Vertex normal
                    Vector3f vec3fNorm = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vec3fNorm);
                    break;
                case "f":
                    if (tokens.length > 4) {
                        // Triangulate the polygon
                        for (int i = 2; i < tokens.length - 1; i++) {
                            faces.add(new Face(tokens[1], tokens[i], tokens[i + 1]));
                        }
                    } else {
                        faces.add(new Face(tokens[1], tokens[2], tokens[3]));
                    }
                    break;
                default:
                    break;
            }
        }

        return reorderLists(vertices, textures, normals, faces);
    }

    private static Mesh reorderLists(
            List<Vector3f> positions,
            List<Vector2f> texCoords,
            List<Vector3f> normals,
            List<Face> faces) {

        List<Float> posArr = new ArrayList<>();
        List<Float> texArr = new ArrayList<>();
        List<Float> normArr = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (Face face : faces) {
            for (IdxGroup idx : face.getFaceVertexIndices()) {

                VertexKey key = new VertexKey(
                        idx.idxPos,
                        idx.idxTextCoord,
                        idx.idxVecNormal
                );

                Integer index = vertexMap.get(key);

                if (index == null) {
                    index = posArr.size() / 3;
                    vertexMap.put(key, index);

                    Vector3f pos = positions.get(idx.idxPos);
                    posArr.add(pos.x);
                    posArr.add(pos.y);
                    posArr.add(pos.z);

                    if (idx.idxTextCoord >= 0) {
                        Vector2f tex = texCoords.get(idx.idxTextCoord);
                        texArr.add(tex.x);
                        texArr.add(1 - tex.y);
                    } else {
                        texArr.add(0f);
                        texArr.add(0f);
                    }

                    if (idx.idxVecNormal >= 0) {
                        Vector3f norm = normals.get(idx.idxVecNormal);
                        normArr.add(norm.x);
                        normArr.add(norm.y);
                        normArr.add(norm.z);
                    } else {
                        normArr.add(0f);
                        normArr.add(0f);
                        normArr.add(0f);
                    }
                }

                indices.add(index);
            }
        }

        return new Mesh(
                toFloatArray(posArr),
                toFloatArray(texArr),
                toFloatArray(normArr),
                indices.stream().mapToInt(i -> i).toArray()
        );
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
