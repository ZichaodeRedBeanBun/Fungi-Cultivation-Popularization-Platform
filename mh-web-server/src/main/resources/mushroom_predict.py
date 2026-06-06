import sys
import json
import os
import numpy as np
from PIL import Image
import torchvision.transforms as transforms
import onnxruntime as ort

def softmax(x):
    e_x = np.exp(x - np.max(x))
    return e_x / e_x.sum()

def predict(image_path, tox_model_path, species_model_path):
    # 加载 ONNX 模型
    tox_session = ort.InferenceSession(tox_model_path)
    species_session = ort.InferenceSession(species_model_path)

    # 预处理（与训练完全一致）
    transform = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225])
    ])
    img = Image.open(image_path).convert('RGB')
    input_tensor = transform(img).unsqueeze(0).numpy().astype(np.float32)

    # 毒性推理
    tox_out = tox_session.run(None, {"input": input_tensor})[0][0]
    tox_probs = softmax(tox_out)
    tox_idx = int(tox_probs.argmax())

    # 毒性类别名称（与训练一致）
    toxicity_classes = ['conditionally_edible', 'edible', 'poisonous']
    tox_name = toxicity_classes[tox_idx]
    tox_conf = float(tox_probs[tox_idx])

    result = {
        "toxicity": tox_name,
        "toxicity_conf": tox_conf,
        "species": None,
        "species_conf": 0.0
    }

    # 如果有毒，进行品种推理
    if tox_name == "poisonous":
        sp_out = species_session.run(None, {"input": input_tensor})[0][0]
        sp_probs = softmax(sp_out)
        sp_idx = int(sp_probs.argmax())

        # 从同目录加载品种类别 JSON
        script_dir = os.path.dirname(os.path.abspath(__file__))
        species_json_path = os.path.join(script_dir, "toxic_species_classes.json")
        with open(species_json_path, "r") as f:
            species_classes = json.load(f)

        sp_name = species_classes[sp_idx]
        sp_conf = float(sp_probs[sp_idx])
        result["species"] = sp_name
        result["species_conf"] = sp_conf

    return result

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print(json.dumps({"error": "需要提供图片路径、毒性模型路径、品种模型路径"}))
        sys.exit(1)
    image_path = sys.argv[1]
    tox_model_path = sys.argv[2]
    species_model_path = sys.argv[3]
    try:
        res = predict(image_path, tox_model_path, species_model_path)
        print(json.dumps(res))
    except Exception as e:
        print(json.dumps({"error": str(e)}))