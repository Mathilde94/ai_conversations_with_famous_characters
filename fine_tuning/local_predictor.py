import requests
import json
import time

class LocalPredictor:
    def __init__(self, port: int = 8080, t: float = 0.9):
        self.url = f"http://127.0.0.1:{port}/completion"
        self.t = t

    def predict(self, payload):
        prompt = payload["inputs"]
        new_tokens = payload["parameters"]["max_new_tokens"]
        r = requests.post(
            self.url,
            data=json.dumps({
                "temperature": self.t,
                "n_predict": new_tokens,
                "stream": False,
                "prompt": prompt,
            }),
            headers={"Content-Type": "application/json"}
        )
        content = json.loads(r.content).get("content")
        return content.strip("\n").split("\n")[0].strip()


class LocalStreamPredictor(LocalPredictor):
    def predict(self, payload):
        prompt = payload["inputs"]
        new_tokens = payload["parameters"]["max_new_tokens"]
        s = requests.Session()
        content = ""
        with s.post(self.url,
                    data=json.dumps({
                        "temperature": self.t,
                        "n_predict": new_tokens,
                        "stream": True,
                        "prompt": prompt,
                    }),
                    headers={"Content-Type": "application/json"}, stream=True) as resp:
            for line in resp.iter_lines():
                if line:
                    added_content = json.loads(line.decode("utf-8").strip("data: ")).get("content")
                    if "\n" in added_content:
                        added_content = added_content.split("\n")[0]
                        print(added_content)
                        content += added_content
                        return content
                    content += added_content
                    print(added_content, end='')
                    time.sleep(0.1)
        return content
