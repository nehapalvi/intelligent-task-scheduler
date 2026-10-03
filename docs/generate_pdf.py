from __future__ import annotations

import re
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (
    Flowable,
    ListFlowable,
    ListItem,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "placement-interview-guide.md"
DESTINATION = ROOT / "placement-interview-guide.pdf"
INK = colors.HexColor("#202923")
TEAL = colors.HexColor("#24786e")
MINT = colors.HexColor("#e8f2dc")
LINE = colors.HexColor("#cbd4c8")


def inline_markup(text: str) -> str:
    code_parts: list[str] = []

    def hold_code(match: re.Match[str]) -> str:
        code_parts.append(f'<font name="Courier" color="#24786e">{escape(match.group(1))}</font>')
        return f"@@CODE{len(code_parts) - 1}@@"

    text = re.sub(r"`([^`]+)`", hold_code, text)
    text = escape(text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<link href="\2" color="#24786e">\1</link>', text)
    for index, part in enumerate(code_parts):
        text = text.replace(f"@@CODE{index}@@", part)
    return text


ENDPOINT = re.compile(
    r"([A-Za-z0-9_*]+)(?:\[\([^\]]*\)\]|\[[^\]]*\]|\(\([^)]*\)\)|\([^)]*\)|\{[^}]*\})?"
)


class MermaidDiagram(Flowable):
    def __init__(self, source: str, available_width: float):
        super().__init__()
        self.available_width = available_width
        self.direction = "TB"
        self.labels: dict[str, str] = {}
        self.edges: list[tuple[str, str]] = []
        self._parse(source)
        self.levels = self._levels()
        self.width = available_width
        self.height = max(100, len(self.levels) * 55 + 10)

    def _parse_endpoint(self, value: str) -> tuple[str, str]:
        value = re.sub(r"^\|[^|]*\|\s*", "", value.strip())
        match = ENDPOINT.match(value)
        if not match:
            return value.strip(), value.strip()
        node_id = match.group(1)
        shape = value[match.end(1):].strip()
        label = node_id
        if shape:
            label = shape.strip("[]{}()").replace("<br/>", " ").replace("<br>", " ")
            label = label.replace('"', "").replace("|", "")
        return node_id, label

    def _parse(self, source: str) -> None:
        for line in source.splitlines():
            line = line.strip()
            if line.startswith("flowchart"):
                self.direction = line.split()[-1]
                continue
            if line.startswith("stateDiagram") or line.startswith("sequenceDiagram"):
                self.direction = "TB"
                continue
            if "-->" not in line:
                continue
            left, right = line.split("-->", 1)
            source_id, source_label = self._parse_endpoint(left)
            target_id, target_label = self._parse_endpoint(right)
            self.labels[source_id] = source_label
            self.labels[target_id] = target_label
            self.edges.append((source_id, target_id))

    def _levels(self) -> list[list[str]]:
        nodes = list(self.labels)
        depth = {node: 0 for node in nodes}
        for _ in range(len(nodes)):
            changed = False
            for source_id, target_id in self.edges:
                candidate = depth[source_id] + 1
                if candidate > depth[target_id]:
                    depth[target_id] = candidate
                    changed = True
            if not changed:
                break
        if nodes and max(depth.values()) >= len(nodes):
            depth = {node: index for index, node in enumerate(nodes)}
        return [[node for node in nodes if depth[node] == level] for level in range(max(depth.values(), default=0) + 1)]

    def draw(self) -> None:
        canvas = self.canv
        box_height = 36
        if self.direction in {"LR", "RL"}:
            column_width = self.width / max(1, len(self.levels))
            positions = {}
            for column, nodes in enumerate(self.levels):
                for row, node in enumerate(nodes):
                    x = column * column_width + column_width / 2
                    y = self.height - 30 - row * 52
                    positions[node] = (x, y)
            box_width = min(130, column_width - 10)
        else:
            positions = {}
            for row, nodes in enumerate(self.levels):
                gap = self.width / (len(nodes) + 1)
                for column, node in enumerate(nodes):
                    x = gap * (column + 1)
                    y = self.height - 25 - row * 52
                    positions[node] = (x, y)
            box_width = min(210, self.width / max((len(level) for level in self.levels), default=1) - 14)

        canvas.setStrokeColor(TEAL)
        canvas.setLineWidth(1.2)
        for source_id, target_id in self.edges:
            if source_id not in positions or target_id not in positions:
                continue
            x1, y1 = positions[source_id]
            x2, y2 = positions[target_id]
            if self.direction in {"LR", "RL"}:
                start_x = x1 + box_width / 2
                end_x = x2 - box_width / 2
                canvas.line(start_x, y1, end_x - 4, y2)
                canvas.line(end_x - 4, y2, end_x - 9, y2 + 3)
                canvas.line(end_x - 4, y2, end_x - 9, y2 - 3)
            else:
                start_y = y1 - box_height / 2
                end_y = y2 + box_height / 2
                canvas.line(x1, start_y, x2, end_y + 4)
                canvas.line(x2, end_y + 4, x2 - 3, end_y + 9)
                canvas.line(x2, end_y + 4, x2 + 3, end_y + 9)

        for node_id, (center_x, center_y) in positions.items():
            canvas.setFillColor(MINT)
            canvas.setStrokeColor(TEAL)
            canvas.roundRect(center_x - box_width / 2, center_y - box_height / 2,
                             box_width, box_height, 5, fill=1, stroke=1)
            label = self.labels[node_id]
            if len(label) > 34:
                midpoint = len(label) // 2
                split = label.rfind(" ", 0, midpoint)
                if split > 0:
                    lines = [label[:split], label[split + 1:]]
                else:
                    lines = [label[:32], label[32:]]
            else:
                lines = [label]
            canvas.setFillColor(INK)
            canvas.setFont("Helvetica", 8)
            line_y = center_y + (len(lines) - 1) * 5
            for line in lines[:2]:
                canvas.drawCentredString(center_x, line_y - 3, line[:40])
                line_y -= 11


def make_styles():
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle("GuideTitle", parent=base["Title"], fontName="Helvetica-Bold",
                                fontSize=25, leading=30, textColor=INK, alignment=TA_LEFT,
                                spaceAfter=10),
        "subtitle": ParagraphStyle("GuideSubtitle", parent=base["Normal"], fontSize=13,
                                    leading=18, textColor=TEAL, spaceAfter=20),
        "h1": ParagraphStyle("GuideH1", parent=base["Heading1"], fontName="Helvetica-Bold",
                             fontSize=18, leading=22, textColor=INK, spaceBefore=14,
                             spaceAfter=7, keepWithNext=True),
        "h2": ParagraphStyle("GuideH2", parent=base["Heading2"], fontName="Helvetica-Bold",
                             fontSize=13, leading=17, textColor=TEAL, spaceBefore=10,
                             spaceAfter=5, keepWithNext=True),
        "h3": ParagraphStyle("GuideH3", parent=base["Heading3"], fontName="Helvetica-Bold",
                             fontSize=10.5, leading=14, textColor=INK, spaceBefore=7,
                             spaceAfter=4, keepWithNext=True),
        "body": ParagraphStyle("GuideBody", parent=base["BodyText"], fontName="Helvetica",
                                fontSize=9, leading=13, textColor=INK, alignment=TA_LEFT,
                                spaceAfter=6),
        "bullet": ParagraphStyle("GuideBullet", parent=base["BodyText"], fontName="Helvetica",
                                  fontSize=9, leading=12, textColor=INK, spaceAfter=1),
        "code": ParagraphStyle("GuideCode", parent=base["Code"], fontName="Courier",
                               fontSize=7.4, leading=9.2, textColor=INK, backColor=colors.HexColor("#f3f6f1"),
                               borderColor=LINE, borderWidth=0.5, borderPadding=7, spaceBefore=4, spaceAfter=8),
        "table": ParagraphStyle("GuideTable", parent=base["BodyText"], fontName="Helvetica",
                                fontSize=7.4, leading=9.5, textColor=INK, spaceAfter=0),
        "table_head": ParagraphStyle("GuideTableHead", parent=base["BodyText"], fontName="Helvetica-Bold",
                                     fontSize=7.5, leading=9.5, textColor=INK, spaceAfter=0),
    }


def parse_markdown(text: str, styles: dict[str, ParagraphStyle], width: float):
    lines = text.splitlines()
    story = []
    index = 0
    first_heading = True
    while index < len(lines):
        raw = lines[index]
        line = raw.strip()
        if not line:
            index += 1
            continue
        if line.startswith("```"):
            language = line[3:].strip()
            index += 1
            block = []
            while index < len(lines) and not lines[index].strip().startswith("```"):
                block.append(lines[index])
                index += 1
            index += 1
            if language == "mermaid":
                diagram = MermaidDiagram("\n".join(block), width)
                story.extend([Spacer(1, 4), diagram, Spacer(1, 6)])
            else:
                code = "\n".join(block)
                story.append(Preformatted(code, styles["code"], maxLineLength=95))
            continue
        if line == "---":
            story.extend([Spacer(1, 6)])
            index += 1
            continue
        if line.startswith("|"):
            rows = []
            while index < len(lines) and lines[index].strip().startswith("|"):
                cells = [cell.strip() for cell in lines[index].strip().strip("|").split("|")]
                if not cells or all(re.fullmatch(r":?-{3,}:?", cell or "-") for cell in cells):
                    index += 1
                    continue
                rows.append(cells)
                index += 1
            if rows:
                column_count = max(map(len, rows))
                data = []
                for row_number, row in enumerate(rows):
                    row += [""] * (column_count - len(row))
                    style = styles["table_head"] if row_number == 0 else styles["table"]
                    data.append([Paragraph(inline_markup(cell), style) for cell in row])
                table = Table(data, colWidths=[width / column_count] * column_count, repeatRows=1, hAlign="LEFT")
                table.setStyle(TableStyle([
                    ("BACKGROUND", (0, 0), (-1, 0), MINT),
                    ("GRID", (0, 0), (-1, -1), 0.35, LINE),
                    ("VALIGN", (0, 0), (-1, -1), "TOP"),
                    ("LEFTPADDING", (0, 0), (-1, -1), 5),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 5),
                    ("TOPPADDING", (0, 0), (-1, -1), 4),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
                ]))
                story.extend([table, Spacer(1, 7)])
            continue
        if line.startswith("# "):
            title = line[2:].strip()
            story.append(Paragraph(inline_markup(title), styles["title"] if first_heading else styles["h1"]))
            first_heading = False
            index += 1
            continue
        if line.startswith("## "):
            title = line[3:].strip()
            if title == "Beginner-to-Advanced Placement Interview Guide":
                story.append(Paragraph(inline_markup(title), styles["subtitle"]))
            else:
                story.append(Paragraph(inline_markup(title), styles["h1"]))
            index += 1
            continue
        if line.startswith("### "):
            story.append(Paragraph(inline_markup(line[4:].strip()), styles["h2"]))
            index += 1
            continue
        if line.startswith("#### "):
            story.append(Paragraph(inline_markup(line[5:].strip()), styles["h3"]))
            index += 1
            continue
        if line.startswith("- "):
            items = []
            while index < len(lines) and lines[index].strip().startswith("- "):
                items.append(ListItem(Paragraph(inline_markup(lines[index].strip()[2:]), styles["bullet"])))
                index += 1
            story.append(ListFlowable(items, bulletType="bullet", leftIndent=15, bulletFontSize=6, spaceAfter=6))
            continue
        if re.match(r"^\d+\.\s", line):
            items = []
            while index < len(lines) and re.match(r"^\d+\.\s", lines[index].strip()):
                item_text = re.sub(r"^\d+\.\s", "", lines[index].strip())
                items.append(ListItem(Paragraph(inline_markup(item_text), styles["bullet"])))
                index += 1
            story.append(ListFlowable(items, bulletType="1", leftIndent=18, spaceAfter=6))
            continue
        paragraph_lines = [line]
        index += 1
        while index < len(lines):
            next_line = lines[index].strip()
            if (not next_line or next_line.startswith(("#", "```", "|", "- ", "---"))
                    or re.match(r"^\d+\.\s", next_line)):
                break
            paragraph_lines.append(next_line)
            index += 1
        story.append(Paragraph(inline_markup(" ".join(paragraph_lines)), styles["body"]))
    return story


def add_page_chrome(canvas, document):
    canvas.saveState()
    page_width, _ = letter
    canvas.setStrokeColor(LINE)
    canvas.line(document.leftMargin, 0.55 * inch, page_width - document.rightMargin, 0.55 * inch)
    canvas.setFillColor(TEAL)
    canvas.setFont("Helvetica", 8)
    canvas.drawString(document.leftMargin, 0.36 * inch, "INTELLIGENT TASK SCHEDULER  |  PLACEMENT GUIDE")
    canvas.setFillColor(INK)
    canvas.drawRightString(page_width - document.rightMargin, 0.36 * inch, f"Page {document.page}")
    canvas.restoreState()


def main() -> None:
    if not SOURCE.exists():
        raise FileNotFoundError(f"Guide source was not found: {SOURCE}")
    styles = make_styles()
    document = SimpleDocTemplate(
        str(DESTINATION),
        pagesize=letter,
        rightMargin=0.62 * inch,
        leftMargin=0.62 * inch,
        topMargin=0.62 * inch,
        bottomMargin=0.78 * inch,
        title="Intelligent Task Scheduler - Placement Interview Guide",
        author="Project Learning Guide",
    )
    story = parse_markdown(SOURCE.read_text(encoding="utf-8"), styles, document.width)
    document.build(story, onFirstPage=add_page_chrome, onLaterPages=add_page_chrome)
    print(f"Created {DESTINATION} ({DESTINATION.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()