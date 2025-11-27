import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/hike.dart';
import 'confirmation_screen.dart';

class AddHikeScreen extends StatefulWidget {
  final Hike? hike;

  const AddHikeScreen({super.key, this.hike});

  @override
  State<AddHikeScreen> createState() => _AddHikeScreenState();
}

class _AddHikeScreenState extends State<AddHikeScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  final _dateController = TextEditingController();
  final _lengthController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _weatherController = TextEditingController();
  final _groupSizeController = TextEditingController();

  bool? _parking;
  String? _difficulty;
  DateTime _selectedDate = DateTime.now();

  @override
  void initState() {
    super.initState();
    if (widget.hike != null) {
      _nameController.text = widget.hike!.name;
      _locationController.text = widget.hike!.location;
      _dateController.text = widget.hike!.date;
      _lengthController.text = widget.hike!.length.toString();
      _descriptionController.text = widget.hike!.description ?? '';
      _weatherController.text = widget.hike!.weather ?? '';
      _groupSizeController.text = widget.hike!.groupSize?.toString() ?? '';
      _parking = widget.hike!.parking;
      _difficulty = widget.hike!.difficulty;
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _locationController.dispose();
    _dateController.dispose();
    _lengthController.dispose();
    _descriptionController.dispose();
    _weatherController.dispose();
    _groupSizeController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );

    if (picked != null) {
      setState(() {
        _selectedDate = picked;
        _dateController.text = DateFormat('dd/MM/yyyy').format(picked);
      });
    }
  }

  void _continueToConfirmation() {
    if (_formKey.currentState!.validate()) {
      if (_parking == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Please select parking availability')),
        );
        return;
      }

      if (_difficulty == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Please select difficulty')),
        );
        return;
      }

      final hike = Hike(
        id: widget.hike?.id,
        name: _nameController.text.trim(),
        location: _locationController.text.trim(),
        date: _dateController.text.trim(),
        parking: _parking!,
        length: double.parse(_lengthController.text.trim()),
        difficulty: _difficulty!,
        description: _descriptionController.text.trim().isEmpty
            ? null
            : _descriptionController.text.trim(),
        weather: _weatherController.text.trim().isEmpty
            ? null
            : _weatherController.text.trim(),
        groupSize: _groupSizeController.text.trim().isEmpty
            ? null
            : int.tryParse(_groupSizeController.text.trim()),
      );

      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => ConfirmationScreen(
            hike: hike,
            isEdit: widget.hike != null,
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.hike == null ? 'Add New Hike' : 'Edit Hike'),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Name
            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Name *',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.title),
              ),
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return 'Name is required';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            // Location
            TextFormField(
              controller: _locationController,
              decoration: const InputDecoration(
                labelText: 'Location *',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.location_on),
              ),
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return 'Location is required';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            // Date
            TextFormField(
              controller: _dateController,
              decoration: const InputDecoration(
                labelText: 'Date *',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.calendar_today),
              ),
              readOnly: true,
              onTap: _selectDate,
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return 'Date is required';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            // Length
            TextFormField(
              controller: _lengthController,
              decoration: const InputDecoration(
                labelText: 'Length (km) *',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.straighten),
              ),
              keyboardType: TextInputType.number,
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return 'Length is required';
                }
                final length = double.tryParse(value.trim());
                if (length == null || length <= 0) {
                  return 'Length must be greater than 0';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            // Parking
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Parking Available *',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: RadioListTile<bool>(
                          title: const Text('Yes'),
                          value: true,
                          groupValue: _parking,
                          onChanged: (value) {
                            setState(() => _parking = value);
                          },
                        ),
                      ),
                      Expanded(
                        child: RadioListTile<bool>(
                          title: const Text('No'),
                          value: false,
                          groupValue: _parking,
                          onChanged: (value) {
                            setState(() => _parking = value);
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Difficulty
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Difficulty *',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  RadioListTile<String>(
                    title: const Text('Easy'),
                    value: 'Easy',
                    groupValue: _difficulty,
                    onChanged: (value) {
                      setState(() => _difficulty = value);
                    },
                  ),
                  RadioListTile<String>(
                    title: const Text('Medium'),
                    value: 'Medium',
                    groupValue: _difficulty,
                    onChanged: (value) {
                      setState(() => _difficulty = value);
                    },
                  ),
                  RadioListTile<String>(
                    title: const Text('Hard'),
                    value: 'Hard',
                    groupValue: _difficulty,
                    onChanged: (value) {
                      setState(() => _difficulty = value);
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Description
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(
                labelText: 'Description (Optional)',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.description),
              ),
              maxLines: 3,
            ),
            const SizedBox(height: 16),

            // Weather
            TextFormField(
              controller: _weatherController,
              decoration: const InputDecoration(
                labelText: 'Weather Expectation (Optional)',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.wb_sunny),
              ),
            ),
            const SizedBox(height: 16),

            // Group Size
            TextFormField(
              controller: _groupSizeController,
              decoration: const InputDecoration(
                labelText: 'Group Size (Optional)',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.group),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 24),

            // Continue Button
            ElevatedButton(
              onPressed: _continueToConfirmation,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 18),
              ),
              child: const Text('Continue'),
            ),
          ],
        ),
      ),
    );
  }
}
